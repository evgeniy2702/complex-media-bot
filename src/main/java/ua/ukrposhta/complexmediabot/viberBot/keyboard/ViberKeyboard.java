package ua.ukrposhta.complexmediabot.viberBot.keyboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is class describe output message from server of viber .
 */

@ToString(of = {"type","defaultHeight","buttons"})
@Getter
public class ViberKeyboard {
    @JsonProperty("Type")
    private final String type = "keyboard";
    @JsonProperty("DefaultHeight")
    private final boolean defaultHeight = false;
    @JsonProperty("Buttons")
    private final List<ReplyViberButton> buttons = new ArrayList<>();

    public ViberKeyboard addButton(ReplyViberButton button) {
        buttons.add(button);
        return this;
    }
}

