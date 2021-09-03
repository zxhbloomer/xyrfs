package com.xyrfs.framework.config.swagger;

import org.springframework.context.annotation.Bean;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class SwaggerConfiguration {

    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
//                   当前包路径
                .apis(RequestHandlerSelectors.basePackage("com.xyrfs"))
                .paths(PathSelectors.any())
                .build();

    }
    //构建api文档的详细信息函数
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                //页面标题
                .title("RESTful API")
                //创建人
                .contact(new Contact("xxxxxxxxxxx","15648260",""))
                //版本号
                .version("1.0")
                //说明
                .description("API 说明")
                .build();
    }
}