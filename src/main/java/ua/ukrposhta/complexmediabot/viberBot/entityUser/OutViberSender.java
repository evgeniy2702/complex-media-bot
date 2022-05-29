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
 * This class describe incoming massage from telegram server to our controller for telegram bot.
 */

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(of={"name", "avatar"})
@ToString
public class OutViberSender {
    private String name;
    @JsonProperty("avatar")
    private String avatar;
}
