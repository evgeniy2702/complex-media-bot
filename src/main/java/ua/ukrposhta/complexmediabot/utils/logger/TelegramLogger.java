package ua.ukrposhta.complexmediabot.utils.logger;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe TELEGRAM type of logger, which write logs to telegram.log.txt file.
 */

public class TelegramLogger extends BotLogger{

    private static volatile TelegramLogger instance;
    private Logger logger = (Logger)LoggerFactory.getLogger(LoggerType.TELEGRAM.getText());

    private TelegramLogger() {}

    public static TelegramLogger getInstance() {
        if (instance == null) {
            synchronized (TelegramLogger.class) {
                if (instance == null) {
                    instance = new TelegramLogger();
                }
            }
        }
        return instance;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}

