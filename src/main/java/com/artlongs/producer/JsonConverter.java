package com.artlongs.producer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJsonMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * <p>Function:</p> 定义 Message 的 JSON 转换器
 * 相比官方的转换器 Jackson2JsonMessageConverter 增加了简单的泛型
 *
 * @version $Revision$ $Date$
 *          Date: 4/22/17
 *          Time: 11:23
 * @author: lqf
 * @since 1.0
 */
public class JsonConverter<T>  extends AbstractJsonMessageConverter {
    private static Log log = LogFactory.getLog(JsonConverter.class);
    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private Jackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();
    private boolean typeMapperSet;

    public JsonConverter() {
        this.initializeJsonObjectMapper();
    }

    public Jackson2JavaTypeMapper getJavaTypeMapper() {
        return this.javaTypeMapper;
    }

    public void setJavaTypeMapper(Jackson2JavaTypeMapper javaTypeMapper) {
        this.javaTypeMapper = javaTypeMapper;
        this.typeMapperSet = true;
    }

    public void setJsonObjectMapper(ObjectMapper jsonObjectMapper) {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public Jackson2JavaTypeMapper.TypePrecedence getTypePrecedence() {
        return this.javaTypeMapper.getTypePrecedence();
    }

    public void setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence typePrecedence) {
        if(this.typeMapperSet) {
            throw new IllegalStateException("When providing your own type mapper, you should set the precedence on it");
        } else if(this.javaTypeMapper instanceof DefaultJackson2JavaTypeMapper) {
            ((DefaultJackson2JavaTypeMapper)this.javaTypeMapper).setTypePrecedence(typePrecedence);
        } else {
            throw new IllegalStateException("Type precedence is available with the DefaultJackson2JavaTypeMapper");
        }
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        super.setBeanClassLoader(classLoader);
        if(!this.typeMapperSet) {
            ((DefaultJackson2JavaTypeMapper)this.javaTypeMapper).setBeanClassLoader(classLoader);
        }

    }

    protected void initializeJsonObjectMapper() {
        this.jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public T fromMessage(Message message) throws MessageConversionException {
        T content = null;
        MessageProperties properties = message.getMessageProperties();
        if(properties != null) {
            String contentType = properties.getContentType();
            if(contentType != null && (contentType.contains("json") || contentType.contains("text/plain")) ) { //json格式
                String encoding = properties.getContentEncoding();
                if(encoding == null) {
                    encoding = this.getDefaultCharset();
                }

                try {
                    if(null == this.getClassMapper()) {
                        JavaType e = this.getJavaTypeMapper().toJavaType(message.getMessageProperties());
                        content = this.convertBytesToObject(message.getBody(), encoding, e);
                    } else {
                        Class e1 = this.getClassMapper().toClass(message.getMessageProperties());
                        content = this.convertBytesToObject(message.getBody(), encoding, e1);
                    }
                } catch (IOException var7) {
                    throw new MessageConversionException("Failed to convert Message content", var7);
                }
            } else {//非JSON格式
                //log.warn("Could not convert incoming message with content-type [" + contentType + "]");
                try {
                    final byte[] inputBit = message.getBody();
                    ObjectInputStream in = new ObjectInputStream(new InputStream() {
                        @Override
                        public int read() throws IOException {
                          return read(inputBit);
                        }
                    });
                } catch (IOException e) {
                    log.error(" read message on error :" + e);
                    return null;
                }
            }
        }

        return content;
    }

    private T convertBytesToObject(byte[] body, String encoding, JavaType targetJavaType) throws IOException {
        String contentAsString = new String(body, encoding);
        return this.jsonObjectMapper.readValue(contentAsString, targetJavaType);
    }

    private T convertBytesToObject(byte[] body, String encoding, Class<?> targetClass) throws IOException {
        String contentAsString = new String(body, encoding);
        return this.jsonObjectMapper.readValue(contentAsString, this.jsonObjectMapper.constructType(targetClass));
    }

    protected Message createMessage(Object objectToConvert, MessageProperties messageProperties) throws MessageConversionException {
        byte[] bytes;
        try {
            String e = this.jsonObjectMapper.writeValueAsString(objectToConvert);
            bytes = e.getBytes(this.getDefaultCharset());
        } catch (IOException var5) {
            throw new MessageConversionException("Failed to convert Message content", var5);
        }

        messageProperties.setContentType("application/json");
        messageProperties.setContentEncoding(this.getDefaultCharset());
        messageProperties.setContentLength((long)bytes.length);
        if(this.getClassMapper() == null) {
            this.getJavaTypeMapper().fromJavaType(this.jsonObjectMapper.constructType(objectToConvert.getClass()), messageProperties);
        } else {
            this.getClassMapper().fromClass(objectToConvert.getClass(), messageProperties);
        }

        return new Message(bytes, messageProperties);
    }
}
