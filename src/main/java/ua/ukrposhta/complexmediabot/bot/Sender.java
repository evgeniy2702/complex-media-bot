package ua.ukrposhta.complexmediabot.bot;

import ua.ukrposhta.complexmediabot.telegramBot.message.OutputMessage;
import ua.ukrposhta.complexmediabot.utils.exception.SenderException;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * Interface for identify send method in classes , which will be
 * to implement it
 */

public interface Sender {

    void send(OutputMessage outputMessage) throws SenderException;
}

