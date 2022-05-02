package ua.ukrposhta.complexmediabot.utils.type;

import java.util.Arrays;
import java.util.List;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is enum of type of buttons, which can use this project.
 */

public enum ButtonType {

    START("/start"),
    UA("Українська"),
    EN("English"),
    REQUEST("Подати запит."),
    REPEAT_REQUEST("Розпочати новий запит."),
    END("Закінчити роботу з ботом."),
    END_WORK("Закінчити роботу з ботом | Finish working with the bot."),
    INSERT_NAME("Вставити ваші зареєстровані дані ?"),
    INSERT_USERNAME("Вставити ваш зареєстрований username ?")
    ;

    private String text;

    ButtonType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public List<ButtonType> getListButtonType(){
        return Arrays.asList(ButtonType.values());
    }
}
