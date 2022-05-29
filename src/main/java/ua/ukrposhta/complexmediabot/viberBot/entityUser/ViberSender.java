package ua.ukrposhta.complexmediabot.viberBot.entityUser;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe one of the field of incoming message from telegram server to our controller for telegram bot.
 * It has call as 'Sender'
 */

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(of={"id", "name"})
@ToString
public class ViberSender {

    // Data from incoming message in "sender" field
    private String id;
    private String name;
    @JsonProperty("avatar")
    private String avatar;
    private String country;
    private String language;
    @JsonProperty("api_version")
    private int api_version;

}
