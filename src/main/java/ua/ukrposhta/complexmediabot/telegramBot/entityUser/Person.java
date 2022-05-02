package ua.ukrposhta.complexmediabot.telegramBot.entityUser;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomingMessage;

import java.time.LocalDateTime;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe entity of user for bot context and saving in temporary cache.
 */

@Data
@ToString
@EqualsAndHashCode(of = "incomingTelegramMessage")
public class Person {

//   from org.telegram.telegrambots.meta.api.objects.Person
    private IncomingMessage incomingTelegramMessage;

    private String currentStateName;
    private String prevStateName;
    private String mediaName;
    private String name_surname;
    private String phone;
    private String email;
    private String subject;
    private String messagePath;
    private String buttonPath;
    private String errorPath;
    private LocalDateTime addDate;
    private LocalDateTime dateUnsubscribe;
    private boolean activity;
    private boolean exit;

    public Person(IncomingMessage incomingTelegramMessage) {
        this.incomingTelegramMessage = incomingTelegramMessage;
        this.currentStateName = "/start";
        this.prevStateName = "";
        this.email="";
        this.mediaName = "";
        this.name_surname = incomingTelegramMessage.getLastName() + " " + incomingTelegramMessage.getFirstName();
        this.phone = "";
        this.subject = "";
        this.messagePath = "";
        this.buttonPath = "";
        this.errorPath = "";
        this.activity = false;
        this.exit = false;
    }
}


