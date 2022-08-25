package ua.ukrposhta.complexmediabot.config;

import com.viber.bot.ViberSignatureValidator;
import com.viber.bot.profile.BotProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import ua.ukrposhta.complexmediabot.bot.BotViber;

import javax.annotation.Nullable;

@Slf4j
@Configuration
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "viberbot")
public class ViberConfig {

    @Value("${viberbot.name}")
    private String viberBotName;
    @Value("${viberbot.token}")
    private String viberBotToken;
    @Value("${viberbot.botPath}")
    private String viberWebhookPath;
    @Nullable
    @Value("${viberbot.avatar:@null}")
    private String avatar;
    @Value("${viberbot.baseUrl}")
    private String viberBotUrl;

    @Autowired
    private Environment env;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient webClient;

    @Bean
    public BotViber viberBot() {
        BotProfile profile = new BotProfile(viberBotName, avatar);
        BotViber bot = new BotViber(profile,viberWebhookPath);
        bot.setViberWebhookPath(viberWebhookPath);
        bot.setViberBotName(viberBotName);
        bot.setViberBotToken(viberBotToken);
        bot.setAvatar(avatar);
        bot.setViberBotUrl(viberBotUrl);
        bot.setRestTemplate(restTemplate);
        log.info("Bot Viber successfully initialized {}, {}, {}, {}", viberBotName, viberBotToken, viberWebhookPath, env.getActiveProfiles()[0]);
        return bot;
    }

    @Bean
    ViberSignatureValidator signatureValidator() {
        return new ViberSignatureValidator(viberBotToken);
    }
}
