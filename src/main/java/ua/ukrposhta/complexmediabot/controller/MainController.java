package ua.ukrposhta.complexmediabot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.ukrposhta.complexmediabot.telegramBot.messageHandler.TelegramIncomingMessageHandler;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.Person;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Controller
 */

@Slf4j
@EnableScheduling
@RestController
@RequestMapping({"/Complex-Media-Bot/","/"})
public class MainController {

    private BotLogger telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);
    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    private final TelegramIncomingMessageHandler telegramIncomingMessageHandler;
    private Map<Long, Person> persons = new HashMap<>();

    public MainController(TelegramIncomingMessageHandler telegramIncomingMessageHandler) {
        this.telegramIncomingMessageHandler = telegramIncomingMessageHandler;
    }
//    private final ObjectMapper objectMapper;
//    private final UserService<Sender, Person, String> userService;
//
//    public MainController(
//            IncomingUpdateService messageProcessor,
//            ObjectMapper objectMapper,
//            @Qualifier("viberSenderUserService")
//                    UserService<Sender, Person, String> userService
//    ) {
//        this.messageProcessor = messageProcessor;
//        this.objectMapper = objectMapper;
//        this.userService = userService;
//    }

    @PostMapping({"telegram",""})
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> receiveTelegramUpdate(@RequestBody Update update ) {
        consoleLogger.info("START receiveTelegramUpdate.method of MainController.class");
        log.info("MainController.class Update : " + update.toString());
        telegramLogger.info("MainController.class Update : " + update.toString());
        telegramIncomingMessageHandler.processingIncomingMessage(update, persons);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public String getDebugMessageGet(){
        return "<center><br>HELLO GET! I`M WORKING!</br></center>";
    }

//    @PostMapping("")
//    public String getDebugMessagePost() {
//        return "<center><br>HELLO POST! I`M WORKING!</br></center>";
//    }


//    @PostMapping("viber")
//    public ResponseEntity<?> receiveViberUpdate(@RequestBody String message) {
//        log.info("Received a message from viber: {}", message);
//        try {
//            JsonNode jsonNode = objectMapper.readTree(message.getBytes(StandardCharsets.UTF_8));
//            JsonNode event = jsonNode.get("event");
//            if (event != null && "conversation_started".equals(event.textValue())) {
//                JsonNode person = jsonNode.get("person");
//                Sender sender = objectMapper.treeToValue(person, Sender.class);
//                messageProcessor.process(sender, Sender.class);
//            } else if (event != null && "unsubscribed".equals(event.textValue())) {
//                String userId = jsonNode.get("user_id").textValue();
//                userService.unsubscribe(userId);
//            } else if (event != null && "message".equals(event.textValue())) {
//                InMessage inMessage = objectMapper.treeToValue(jsonNode, InMessage.class);
//                messageProcessor.process(inMessage, InMessage.class);
//            } else {
//                log.warn("Received unprocessed event from viber - {}", event);
//            }
//        } catch (Exception e) {
//            log.error("Error while proceeding viber message.", e);
//        }
//        return ResponseEntity.ok().build();
//    }

    @Scheduled(cron = "${cron.expression}")
    public void cleanCachePersonsMap(){
        persons.values().forEach(person -> {
            LocalDateTime now = LocalDateTime.now();
            if( (now.getDayOfYear() - person.getAddDate().getDayOfYear()) <= 1)
                persons.remove(person.getIncomingTelegramMessage().getChat_id());
        });
    }
}

