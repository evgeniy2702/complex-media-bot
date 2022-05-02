package ua.ukrposhta.complexmediabot.utils.handlerXml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is abstract class for describing Handler of data according type of data from xml file.
 */

@Setter
@Getter
@Component
public abstract class AbstractHandlerXml<T, R> extends DefaultHandler {

    BotLogger consoleLogger  = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    final String ID_TAG = "id";
    final String TYPE_TAG = "type";
    final String TXT_TAG = "txt";

    T list;
    R entity;
    String exTag;
    String botType;


    public BotLogger getLogger(String botType){
        return BotLogger.getLogger(LoggerType.valueOf(botType));
    }

    public T getListOfEntities(){
        return  list;
    }
    @Override
    public abstract void startDocument() throws SAXException;

    @Override
    public abstract void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException;

    @Override
    public abstract void characters(char[] ch, int start, int length) throws SAXException;

    @Override
    public abstract void endElement(String uri, String localName, String qName) throws SAXException;

}
