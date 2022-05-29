package ua.ukrposhta.complexmediabot.viberBot.keyboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is abstract class describe model of button for keyboard of viber bot .
 */

@Setter
@ToString
public abstract class ViberButton {
    @JsonProperty("Columns")
    protected int columns;
    @JsonProperty("Rows")
    protected int rows;
    @JsonProperty("Text")
    protected String text;
    @JsonProperty("BgColor")
    protected String bgColor = "#fff126";
    @JsonProperty("TextSize")
    protected String textSize = "regular";
    @JsonProperty("TextHAlign")
    protected String textHAlign = "center";
}

