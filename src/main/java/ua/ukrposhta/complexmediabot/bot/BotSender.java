package ua.ukrposhta.complexmediabot.bot;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.ukrposhta.complexmediabot.model.OutputMessage;
import ua.ukrposhta.complexmediabot.utils.exception.SenderException;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.util.List;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class realises Sender interface and identify sender by type of bot
 * for realises send method throw bot according type
 */

@Service
@AllArgsConstructor
public class BotSender implements Sender {

    private final List<TypedSender> senders;

    @Override
    public void send(OutputMessage outputMessage) throws SenderException {
        BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);
        BotLogger logger = BotLogger.getLogger(LoggerType.valueOf(outputMessage.getContext().getTypeBot().name()));

        consoleLogger.info("START send method in BotSender.class");
        logger.info("Trying to send message - " + outputMessage);
        for (TypedSender sender : senders) {
            if (outputMessage.getContext().getTypeBot() == sender.getBotType()) {
                logger.info("Found sender with type -  " + sender.getBotType());
                sender.send(outputMessage);
                if(sender.getBotType() == BotType.TELEGRAM) {
                    logger.info("message successfully send to telegramPerson - " + outputMessage.getContext().getTelegramPerson()
                            .getIncomTelegramMessage().getChat_id());
                    return;
                }
                if(sender.getBotType() == BotType.VIBER) {
                    logger.info("message successfully send to viberPerson - " + outputMessage.getContext().getViberPerson()
                            .getViberSender().getId());
                    return;
                }

            }
        }
        logger.warn("Sender not found for type - " + outputMessage.getContext().getTypeBot());
    }
}

