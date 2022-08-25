package ua.ukrposhta.complexmediabot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.ukrposhta.complexmediabot.model.user.PersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.messageHandler.TelegramIncomingMessageHandler;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ViberLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.viberBot.messageHandler.ViberIncomingMessageHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Controller
 */

@Slf4j
@RestController
@RequestMapping({"/Complex-Media-Bot/","/"})
public class MainController {

    private BotLogger telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);
    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);
    private BotLogger viberLogger = ViberLogger.getLogger(LoggerType.VIBER);

    private final TelegramIncomingMessageHandler telegramIncomingMessageHandler;
    private final ViberIncomingMessageHandler viberIncomingMessageHandler;
    private Map<String, PersonEntity> persons = new HashMap<>();
    private final ObjectMapper objectMapper;

    public MainController(TelegramIncomingMessageHandler telegramIncomingMessageHandler,
                          ViberIncomingMessageHandler viberIncomingMessageHandler,
                          ObjectMapper objectMapper) {
        this.telegramIncomingMessageHandler = telegramIncomingMessageHandler;
        this.viberIncomingMessageHandler = viberIncomingMessageHandler;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = {"telegram"})
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> receiveTelegramUpdate(@RequestBody Update update ) throws JsonProcessingException {
        consoleLogger.info("START receiveTelegramUpdate.method of MainController.class");
        log.info("MainController.class Update : " + update.toString());
        telegramLogger.info("MainController.class Update : " + update.toString());
        telegramIncomingMessageHandler.processingIncomingMessage(update, persons);
        return ResponseEntity.ok().build();
    }

    @Async
    @PostMapping(value = {"viber", ""}, produces = "application/json")
    public ResponseEntity<?> receiveViberUpdate(@RequestBody String message,
                                     @RequestHeader("X-Viber-Content-Signature") String serverSideSignature) throws IOException, ExecutionException, InterruptedException {
        consoleLogger.info("START receiveViberUpdate.method of MainController.class");
        log.info("Received a message from viber: {} , header X-Viber-Content-Signature : {}", message, serverSideSignature);
        viberLogger.info("MainController.class Message : " + message + ", header X-Viber-Content-Signature : " + serverSideSignature);
        try {
            JsonNode json = objectMapper.readTree(message.getBytes(StandardCharsets.UTF_8));
            JsonNode event = json.get("event");
            if (event != null && "conversation_started".equals(event.textValue())) {

                viberIncomingMessageHandler.processingIncomingMessage(json, persons);

            } else if (event != null && "unsubscribed".equals(event.textValue())) {

                viberIncomingMessageHandler.processingIncomingMessage(json, persons);

            } else if (event != null && "message".equals(event.textValue())) {

                CompletableFuture.supplyAsync(() -> {
                    try {
                        viberIncomingMessageHandler.processingIncomingMessage(json, persons);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return ResponseEntity.ok().build();
                });

            } else {
                log.warn("Received unprocessed event from viber - {}", event);
                viberLogger.warn("Received unprocessed event from viber - " + event);
            }
        } catch (Exception e) {
            log.error("Error while proceeding viber message.", e);
            viberLogger.error("ERROR while proceeding viber message." + e.getMessage());
            viberLogger.error("CAUSE while proceeding viber message." + e.getCause());
        }
        return ResponseEntity.ok().build();
    }
}

