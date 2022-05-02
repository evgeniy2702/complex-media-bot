package ua.ukrposhta.complexmediabot.utils.exception;


import java.util.Objects;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class of Exception "Message not provided".
 */

public class MessageNotProvidedException extends RuntimeException {

    private static final String MESSAGE = "Not provided message for %s by %s";

    public MessageNotProvidedException(String message, String str) {
        super(String.format(MESSAGE, Objects.requireNonNull(message), Objects.requireNonNull(str)));
    }
}

