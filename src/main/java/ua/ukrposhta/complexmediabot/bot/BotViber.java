package ua.ukrposhta.complexmediabot.bot;

import com.google.common.util.concurrent.ListenableFuture;
import com.viber.bot.api.ApiResponse;
import com.viber.bot.api.MessageDestination;
import com.viber.bot.api.ViberBot;
import com.viber.bot.message.Message;
import com.viber.bot.profile.BotProfile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import ua.ukrposhta.complexmediabot.model.OutputMessage;
import ua.ukrposhta.complexmediabot.utils.exception.SenderException;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ViberLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ViberTypeMessage;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.OutViberSender;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;
import ua.ukrposhta.complexmediabot.viberBot.message.ViberOutMessage;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class BotViber extends ViberBot implements TypedSender {

    private String viberBotName;
    private String viberBotToken;
    private String viberWebhookPath;
    private String avatar;
    private String viberBotUrl;
    private RestTemplate restTemplate;

    private Map<String, ViberPersonEntity> viberPersons = new HashMap<>();
    private Map<String, String> piars = new HashMap<>();
    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);
    private BotLogger viberLogger = ViberLogger.getLogger(LoggerType.VIBER);


    public BotViber(BotProfile profile, String viberWebhookPath) {
        super(profile, viberWebhookPath);
        super.setWebhook(viberWebhookPath);
    }


    @Override
    public ListenableFuture<ApiResponse> setWebhook(@Nonnull String url) {
        return super.setWebhook(url);
    }

    @Override
    public ListenableFuture<Collection<String>> sendMessage(@Nonnull MessageDestination to, @Nonnull Message... messages) {
        return super.sendMessage(to, messages);
    }

    @Override
    public BotProfile getBotProfile() {
        return super.getBotProfile();
    }

    @Override
    public BotType getBotType() {
        return BotType.VIBER;
    }

    @Override
    public void send(OutputMessage outputMessage) throws SenderException {
        String finalUrl = viberBotUrl +
                (viberBotUrl.endsWith("/") ? ViberTypeMessage.SEND_MESSAGE.getText() : "/" + ViberTypeMessage.SEND_MESSAGE.getText());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=utf-8");
            headers.add("X-Viber-Auth-Token", viberBotToken);

            HttpEntity<ViberOutMessage> request = new HttpEntity<>(toViberOutMessage(outputMessage), headers);

            log.info("BotViber.class send method request : " + request);
            viberLogger.info("BotViber.class send method request : " + request);

            restTemplate.postForEntity(
                    finalUrl,
                    request,
                    String.class
            );

        } catch (Exception e) {
            log.error("BotViber.class Error {} while sending viber message: {}", e, e.getMessage());
            viberLogger.error("BotViber.class Error " + e + " while sending viber message: " + e.getMessage() + ", cause is : " + e.getCause());
            consoleLogger.error("BotViber.class Error " + e + " while sending viber message: " + e.getMessage() + ", cause is : " + e.getCause());
            throw new SenderException(BotType.VIBER);
        }
    }

    private ViberOutMessage toViberOutMessage(OutputMessage outputMessage) {
        OutViberSender outSender = OutViberSender.builder()
                .name(outputMessage.getContext().getViberPerson().getViberSender().getName())
                .avatar(outputMessage.getContext().getViberPerson().getViberSender().getAvatar())
                .build();

        ViberOutMessage message = ViberOutMessage.builder()
                .minApiVersion(outputMessage.getContext().getViberPerson().getViberSender().getApi_version())
                .receiver(outputMessage.getContext().getViberPerson().getViberSender().getId())
                .text(outputMessage.getMessage_text())
                .keyboard(outputMessage.getViberKeyboard())
                .outSender(outSender)
                .build();
        viberLogger.info("BotViber.class toViberOutMessage method OutViberSender : " + message.getOutSender().toString());
        log.info("BotViber.class toViberOutMessage method OutViberSender : " + message.getOutSender().toString());
        viberLogger.info("BotViber.class toViberOutMessage method ViberOutMessage : " + message.toString());
        log.info("BotViber.class toViberOutMessage method ViberOutMessage : " + message.toString());
        return message;



    }
}
