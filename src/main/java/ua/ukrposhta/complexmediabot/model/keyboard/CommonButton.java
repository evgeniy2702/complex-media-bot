package ua.ukrposhta.complexmediabot.model.keyboard;

import lombok.*;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class for describe and create buttons for output message to telegram bot.
 */

@Getter
@Setter
@ToString
public class CommonButton implements Comparable<CommonButton>  {

    int id;
    String text;
    private  String callbackData;

    @Override
    public int compareTo(CommonButton o) {
        return Integer.compare(id, o.getId());
    }
}

