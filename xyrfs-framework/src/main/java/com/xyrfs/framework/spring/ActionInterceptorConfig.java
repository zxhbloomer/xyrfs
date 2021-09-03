package com.xyrfs.framework.spring;

import com.xyrfs.framework.config.messageconverter.CallbackMappingJackson2HttpMessageConverter;
import com.xyrfs.framework.spring.interceptor.impl.ActionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author zxh
 */
@Configuration
public class ActionInterceptorConfig implements WebMvcConfigurer {
//public class ActionInterceptorConfig extends WebMvcConfigurationSupport {

    /**
     * 处理拦截器，主要处理controller中的处理前，中，后
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //  /**表示拦截所有请求
        String[] addPath = {"/**"};
        // 放行getStudent和getUser两个请求：其中getUser请求没有对应的处理器
        String[] excludePath = {"/getStudent","/getUser"};
        registry.addInterceptor(new ActionInterceptor()).addPathPatterns(addPath).excludePathPatterns(excludePath);
    }

    /**
     *
     * @param returnValueHandlers
     */
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        // 处理返回值
//        WebMvcConfigurer.super.addReturnValueHandlers(returnValueHandlers);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //        converters.add(0, new CallbackMappingJackson2HttpMessageConverter());
        CallbackMappingJackson2HttpMessageConverter converter = new CallbackMappingJackson2HttpMessageConverter();
        converter.setDefaultCharset(Charset.forName("UTF-8"));
        converters.add(0, converter);

//        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
//        while(iterator.hasNext()){
//            HttpMessageConverter<?> converter = iterator.next();
//            if(converter instanceof MappingJackson2HttpMessageConverter){
//                iterator.remove();
//            }
//        }
//        CallbackMappingJackson2HttpMessageConverter callbackMappingJackson2HttpMessageConverter = new CallbackMappingJackson2HttpMessageConverter();
//        converters.add(callbackMappingJackson2HttpMessageConverter);

//        //调用父类的配置
//        WebMvcConfigurer.super.configureMessageConverters(converters);
//        //创建fastjson消息转换器
//        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
//        // 中文乱码解决方案
//        List<MediaType> mediaTypes = new ArrayList<>();
//        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);//设定json格式且编码为UTF-8
//        converter.setSupportedMediaTypes(mediaTypes);
//
//        //创建fastjson配置类
//        FastJsonConfig config = new FastJsonConfig();
//        /**
//         *
//         * QuoteFieldNames———-输出key时是否使用双引号,默认为true
//         * WriteMapNullValue——–是否输出值为null的字段,默认为false
//         * WriteNullNumberAsZero—-数值字段如果为null,输出为0,而非null
//         * WriteNullListAsEmpty—–List字段如果为null,输出为[],而非null
//         * WriteNullStringAsEmpty—字符类型字段如果为null,输出为”“,而非null
//         * WriteNullBooleanAsFalse–Boolean字段如果为null,输出为false,而非null
//         *
//         * */
//        //修改配置返回内容的过滤
//        config.setSerializerFeatures(
//                SerializerFeature.DisableCircularReferenceDetect,
//                SerializerFeature.WriteNullStringAsEmpty,
//                SerializerFeature.WriteNullListAsEmpty,
//                SerializerFeature.WriteNullBooleanAsFalse,
//                SerializerFeature.PrettyFormat,
//                SerializerFeature.WriteNullNumberAsZero
//        );
//        converter.setFastJsonConfig(config);
//        //将fastjson添加到视图消息转换器列表内
//        converters.add(0,converter);


    }
}
