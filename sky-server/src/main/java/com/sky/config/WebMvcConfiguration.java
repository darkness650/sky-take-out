package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/employee/login",
                        "/doc.html",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/swagger-resources/**"
                );
        registry.addInterceptor(jwtTokenUserInterceptor).addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login","/user/shop/status");
    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */

    @Bean
    public OpenAPI openAPI() {
        log.info("开始注册Knife4j...");
        return new OpenAPI()
                .info(new Info()
                        .title("苍穹外卖项目接口文档")
                        .version("2.0").
                        description("苍穹外卖项目接口文档"));
    }


    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-resources/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        // 这样做会导致swagger的json数据无法解析
//        // 创建一个消息转换器对象
//        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
//        // 需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
//        converter.setObjectMapper(new JacksonObjectMapper());
//        // 将自己的消息转化器加入容器中（放到最前面）
//        converters.add(converter);

        // 避免新创建converter
        for(HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter j2httpMessageConverter) {
                log.info("已存在MappingJackson2HttpMessageConverter，进行修改");
                // 设置对象转换器
                j2httpMessageConverter.setObjectMapper(new JacksonObjectMapper());
                // 退出
                return;
            }
        }
        // 否则再创建新的消息转换器对象
        log.info("不存在MappingJackson2HttpMessageConverter，创建新的");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.addFirst(converter);
    }
}
