package cn.bp.scada.config.token;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    // WebMvcConfigurerAdapter 这个类在SpringBoot2.0已过时，官方推荐直接实现WebMvcConfigurer 这个接口

    @Bean
    public JwtInterceptor jwtInterceptor() {

        return new JwtInterceptor();

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration jwtInterceptorRegistration = registry.addInterceptor(jwtInterceptor());
        // 配置拦截器的拦截规则和放行规则
        // 一个*：只匹配字符，不匹配路径（/）
        //两个**：匹配字符，和路径（/）
        jwtInterceptorRegistration.addPathPatterns("/**")  //拦截所有的请求
                //.excludePathPatterns("/scada/**","/jwt/login"); //指定不拦截的url地址 ,页面url的path
        .excludePathPatterns("/**");
     /*   jwtInterceptorRegistration.addPathPatterns("/**")  //拦截所有的请求
                .excludePathPatterns("/jwt/login");*/
    }
}
