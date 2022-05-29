package ua.ukrposhta.complexmediabot.viberBot.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.OutViberSender;
import ua.ukrposhta.complexmediabot.viberBot.keyboard.ViberKeyboard;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is class describe output message from server of viber .
 */

@Setter
@Getter
@Builder(toBuilder = true)
@ToString(of = {"receiver","minApiVersion","outSender","type","text","keyboard"})
public class ViberOutMessage {

    private String receiver;
    @Builder.Default
    @JsonProperty("min_api_version")
    private Integer minApiVersion = 3;
    private final String type = "text";
    private String text;
    private ViberKeyboard keyboard;
    @JsonProperty("sender")
    private OutViberSender outSender;

    public void cleanUp() {
        keyboard = null;
        receiver = null;
        text = null;
    }

}
