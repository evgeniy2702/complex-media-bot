package ua.ukrposhta.complexmediabot.viberBot.entityUser;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ua.ukrposhta.complexmediabot.model.user.PersonEntity;

@Data
@ToString(of = "viberSender", callSuper = true)
@EqualsAndHashCode(of = "viberSender", callSuper = true)
public class ViberPersonEntity extends PersonEntity {

//   from com.viber.bot.message.Message
    private ViberSender viberSender;

    public ViberPersonEntity(ViberSender viberSender) {
        super.setCurrentStateName("/start");
        super.setPrevStateName("");
        super.setEmail("");
        super.setMediaName("");
        super.setName_surname(viberSender.getName());
        super.setPhone("");
        super.setSubject("");
        super.setMessagePath("");
        super.setButtonPath("");
        super.setErrorPath("");
        super.setActivity(false);
        super.setExit(false);
        this.viberSender = viberSender;
    }
}
