package ua.ukrposhta.complexmediabot.utils.type;

import lombok.Getter;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is enum of types of loggers, which can use this project.
 */

@Getter
public enum LoggerType {
    CONSOLE("consoleLogger"),
    TELEGRAM("telegramLogger"),
    VIBER("viberLogger");

    private String text;

    LoggerType(String text) {
        this.text = text;
    }
}
