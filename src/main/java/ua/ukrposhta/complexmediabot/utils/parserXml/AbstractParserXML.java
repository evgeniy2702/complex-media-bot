package ua.ukrposhta.complexmediabot.utils.parserXml;

import lombok.Data;
import org.xml.sax.SAXException;
import ua.ukrposhta.complexmediabot.utils.handlerXml.AbstractHandlerXml;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is abstract class for describing Parser of data according type of data from xml file.
 */

@Data
public class AbstractParserXML<T , R extends AbstractHandlerXml> {

    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    private BotLogger telegramTelegram = TelegramLogger.getLogger(LoggerType.TELEGRAM);

    private BotLogger viberTelegram = TelegramLogger.getLogger(LoggerType.VIBER);

    @SuppressWarnings("unchecked")
    public T getObjectExchangeFromXML(String uri, R handler){
        consoleLogger.info("----------------------------------------------------------------");
        consoleLogger.info("start getObjectExchangeFromXML method in " + getClass() );

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            File xmlFile = new File(getClass().getClassLoader().getResource(uri).getFile());
            parser.parse(xmlFile, handler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            telegramTelegram.error("AbstractParserXML.class ERROR : " + e.getMessage() + "/n CAUSE : " + e.getCause());
            viberTelegram.error("AbstractParserXML.class ERROR : " + e.getMessage() + "/n CAUSE : " + e.getCause());
        }

        return (T) handler.getListOfEntities();
    }
}
