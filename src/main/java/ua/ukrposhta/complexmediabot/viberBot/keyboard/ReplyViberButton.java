package ua.ukrposhta.complexmediabot.viberBot.keyboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is class for create keyboard and fill it by buttons according state of user .
 */

@Getter
@ToString(of={"actionType","actionBody"},callSuper = true)
public class ReplyViberButton extends ViberButton {
    @JsonProperty("ActionType")
    private String actionType = "reply";
    @JsonProperty("ActionBody")
    private String actionBody;

    public ReplyViberButton(int columns, int rows, String text, String actionBody) {
        this.columns = columns;
        this.rows = rows;
        this.text = text;
        this.actionBody = actionBody;
    }

    public void setActionBody(String actionBody) {
        this.actionBody = actionBody;
    }
}

