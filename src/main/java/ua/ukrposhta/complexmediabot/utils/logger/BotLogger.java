package ua.ukrposhta.complexmediabot.utils.logger;

import ch.qos.logback.classic.Logger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This abstract class for drive by type of Logger.
 */


public abstract class BotLogger {

    public static BotLogger getLogger(LoggerType type) {
        BotLogger logger;
        switch (type) {
            case TELEGRAM:
                logger = TelegramLogger.getInstance();
                break;
            case VIBER:
                logger = ViberLogger.getInstance();
                break;
            case CONSOLE:
                logger = ConsoleLogger.getInstance();
                break;
            default:
                throw new IllegalArgumentException("Can't resolve logger type!");
        }
        return logger;
    }

    public void info(String message) {
        getLogger().info(message);
    }
    public void warn(String message) { getLogger().warn(message);}
    public void error(String message) {
        getLogger().error(message);
    }

    protected abstract Logger getLogger();
}
