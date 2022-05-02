package ua.ukrposhta.complexmediabot.utils.logger;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe CONSOLE type of logger, which write logs on console.
 */

public class ConsoleLogger extends BotLogger {

    private static volatile ConsoleLogger instance;
    private Logger logger = (Logger)LoggerFactory.getLogger(LoggerType.CONSOLE.getText());

    private ConsoleLogger() {}

    public static ConsoleLogger getInstance() {
        if (instance == null) {
            synchronized (ConsoleLogger.class) {
                if (instance == null) {
                    instance = new ConsoleLogger();
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
