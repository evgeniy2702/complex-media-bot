package ua.ukrposhta.complexmediabot.viberBot.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberSender;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is class describe input message from server of viber .
 */


@Getter
@ToString
public class ViberInMessage {

    private String event = "message";
    private long timestamp;
    @JsonProperty("message_token")
    private String messageToken;
    private ViberSender sender;
    @JsonProperty("message")
    private MessageIN message;
    private boolean silent = false;
    @JsonProperty("chat_hostname")
    private String chatHostname;

}

