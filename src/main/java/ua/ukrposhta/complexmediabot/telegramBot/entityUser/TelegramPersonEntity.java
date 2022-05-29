package ua.ukrposhta.complexmediabot.telegramBot.entityUser;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ua.ukrposhta.complexmediabot.model.user.PersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomTelegramMessage;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe entity of user for bot context and saving in temporary cache.
 */

@Data
@ToString
@EqualsAndHashCode(of = "incomTelegramMessage", callSuper = false)
public class TelegramPersonEntity extends PersonEntity {

//   from org.telegram.telegrambots.meta.api.objects.User
    private IncomTelegramMessage incomTelegramMessage;

    public TelegramPersonEntity(IncomTelegramMessage incomTelegramMessage) {
        super.setCurrentStateName("/start");
        super.setPrevStateName("");
        super.setEmail("");
        super.setMediaName("");
        super.setName_surname(incomTelegramMessage.getLastName() + " " + incomTelegramMessage.getFirstName());
        super.setPhone("");
        super.setSubject("");
        super.setMessagePath("");
        super.setButtonPath("");
        super.setErrorPath("");
        super.setActivity(false);
        super.setExit(false);
        this.incomTelegramMessage = incomTelegramMessage;
    }
}


