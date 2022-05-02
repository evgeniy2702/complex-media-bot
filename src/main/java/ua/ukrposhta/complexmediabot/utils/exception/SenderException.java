package ua.ukrposhta.complexmediabot.utils.exception;

import ua.ukrposhta.complexmediabot.utils.type.BotType;

import java.util.Objects;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class of Exception , which can be in process of send message to telegram server.
 */

public class SenderException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Error sending message out for sender: %s";

    public SenderException(String message) {
        super(message);
    }

    public SenderException(BotType botType) {
        super(String.format(DEFAULT_MESSAGE, Objects.requireNonNull(botType)));
    }

    public SenderException(BotType botType, Throwable cause) {
        super(String.format(DEFAULT_MESSAGE, Objects.requireNonNull(botType)), cause);
    }

    public SenderException(String message, BotType botType) {
        super(message + ". BotType - " + botType);
    }

    public SenderException(String message, Throwable cause) {
        super(message, cause);
    }
}

