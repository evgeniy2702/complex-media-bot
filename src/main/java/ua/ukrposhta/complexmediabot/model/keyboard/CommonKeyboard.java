package ua.ukrposhta.complexmediabot.model.keyboard;

import lombok.Getter;
import lombok.ToString;
import ua.ukrposhta.complexmediabot.utils.type.KeyboardType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class to describe and create keyboard for output message to telegram bot.
 */

@Getter
@ToString(of = {"id", "name"})
public class CommonKeyboard {

    private Integer id;
    private String name;

    private final List<CommonRow> rowList = new ArrayList<>();

    private KeyboardType keyboardType = KeyboardType.REPLY;

    public CommonKeyboard setKeyboardType(KeyboardType keyboardType) {
        this.keyboardType = keyboardType;
        return this;
    }

    public void addRow(CommonRow row) {
        rowList.add(row);
    }

    public int size() {
        return rowList.size();
    }
}
