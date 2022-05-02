package ua.ukrposhta.complexmediabot.model.keyboard;

import lombok.Getter;
import lombok.ToString;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class to describe and create keyboard rows of buttons for output message to telegram bot.
 */

@Getter
@ToString
public class CommonRow implements Iterable<CommonButton> {

    private int id;
    private final Set<CommonButton> buttonList = new TreeSet<>();

    public void addButton(CommonButton button) {
        buttonList.add(button);
    }

    public boolean isEmpty() {
        return buttonList.isEmpty();
    }

    @Override
    public Iterator<CommonButton> iterator() {
        return buttonList.iterator();
    }
}

