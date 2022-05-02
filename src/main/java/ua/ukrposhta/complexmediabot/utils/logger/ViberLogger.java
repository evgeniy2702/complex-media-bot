package ua.ukrposhta.complexmediabot.utils.logger;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe TELEGRAM type of logger, which write logs to viber.log.txt file.
 */

public class ViberLogger extends BotLogger{

    private static volatile ViberLogger instance;
    private Logger logger = (Logger) LoggerFactory.getLogger(LoggerType.VIBER.getText());

    private ViberLogger() {}

    public static ViberLogger getInstance() {
        if (instance == null) {
            synchronized (ViberLogger.class) {
                if (instance == null) {
                    instance = new ViberLogger();
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

