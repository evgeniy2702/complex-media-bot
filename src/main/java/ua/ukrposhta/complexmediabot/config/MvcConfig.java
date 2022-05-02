package ua.ukrposhta.complexmediabot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;

import java.time.Duration;
import java.util.Arrays;

/**
 * @author Zhurenko Evgeniy
 *
 */

@Slf4j
@Configuration
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "telegrambot")
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Value("${telegrambot.ProxyType}")
    private String proxyType;
    @Value("${telegrambot.ProxyHost}")
    private String proxyHost;
    @Value("${telegrambot.ProxyPort}")
    private String proxyPort;
    @Value("${telegrambot.name}")
    private String botUserName;
    @Value("${telegramBot.botPath}")
    private String botPath;
    @Value("${telegrambot.token}")
    private String botToken;
    @Autowired
    private Environment env;

    @Bean
    public TelegramBot telegramBot() {
        log.info("Start initiating bot");
        DefaultBotOptions options = new DefaultBotOptions();
        TelegramBot bot = new TelegramBot(options);
        bot.setBotUserName(botUserName);
        bot.setBotToken(botToken);
        bot.setBotPath(botPath);
        if( Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("dev"))) {
            options.setProxyType(ProxyType.valueOf(proxyType));
            options.setProxyHost(proxyHost);
            options.setProxyHost(proxyPort);
        }
        log.info("Bot successfully initialized {}, {}, {}, {}", botUserName, botToken, botPath, env.getActiveProfiles()[0]);
        return bot;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        registry.addResourceHandler("/**.css").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/**.js").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/**.html").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/**.properties").addResourceLocations("classpath:/properties/");
    }
}
