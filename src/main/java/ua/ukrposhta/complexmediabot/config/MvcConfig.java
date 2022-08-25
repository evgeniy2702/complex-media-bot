package ua.ukrposhta.complexmediabot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Zhurenko Evgeniy
 *
 */

@Slf4j
@Configuration
@EnableConfigurationProperties
@EnableWebMvc
@EnableAsync
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        registry.addResourceHandler("/**.css").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/**.js").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/**.html").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/**.properties").addResourceLocations("classpath:/properties/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }
}
