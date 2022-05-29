package ua.ukrposhta.complexmediabot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;

import java.util.Arrays;

@Slf4j
@Configuration
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "telegrambot")
public class TelegramConfig {

    @Value("${telegrambot.ProxyType}")
    private String proxyType;
    @Value("${telegrambot.ProxyHost}")
    private String proxyHost;
    @Value("${telegrambot.ProxyPort}")
    private String proxyPort;
    @Value("${telegrambot.name}")
    private String telegramBotName;
    @Value("${telegramBot.botPath}")
    private String telegramWebhookPath;
    @Value("${telegrambot.token}")
    private String telegramBotToken;


    @Autowired
    private Environment env;

    @Bean
    public TelegramBot telegramBot() {
        log.info("Start initiating bot");
        DefaultBotOptions options = new DefaultBotOptions();
        TelegramBot bot = new TelegramBot(options);
        bot.setTelegramBotName(telegramBotName);
        bot.setTelegramBotToken(telegramBotToken);
        bot.setTelegramWebhookPath(telegramWebhookPath);
        if( Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("dev"))) {
            options.setProxyType(ProxyType.valueOf(proxyType));
            options.setProxyHost(proxyHost);
            options.setProxyHost(proxyPort);
        }
        log.info("Bot successfully initialized {}, {}, {}, {}", telegramBotName, telegramBotToken, telegramWebhookPath, env.getActiveProfiles()[0]);
        return bot;
    }
}
