package ua.ukrposhta.complexmediabot.viberBot.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is class describe a field of input message from server of viber, that call "message" .
 */

@Getter
@ToString
public class MessageIN {

    private String type = "text";
    private String text;
    @JsonProperty("tracking_data")
    private String tracking_data;
}
