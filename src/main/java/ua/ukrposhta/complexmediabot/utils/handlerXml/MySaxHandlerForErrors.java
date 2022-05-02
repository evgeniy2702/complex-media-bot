package ua.ukrposhta.complexmediabot.utils.handlerXml;

import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntity;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.util.ArrayList;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Handler. It handle data from errors.xml file.
 */

@Component
public class MySaxHandlerForErrors extends AbstractHandlerXml<ErrorEntityList, ErrorEntity> {

    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    private final String ERROR_TAG = "error";

    public ErrorEntityList getList() {
        return list;
    }

    public void setBotType(String botType) {
        this.botType = botType;
    }

    public BotLogger getLogger(String botType){
        return BotLogger.getLogger(LoggerType.valueOf(botType));
    }

    @Override
    public void startDocument() throws SAXException {
        consoleLogger.info("----------------------------------------------------------");
        consoleLogger.info("start startDocument method in MySaxHandlerForErrors.class");

        list = new ErrorEntityList();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        exTag = qName;
        final String ERRORS_TAG = "errors";

        switch (exTag){
            case ERRORS_TAG: {
                list.setErrorEntities(new ArrayList<>());
            }
            case ERROR_TAG: entity = new ErrorEntity();
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        String text = new String(ch,start,length);

        if(text.contains("<") || exTag == null){
            return;
        }
        switch (exTag){
            case ID_TAG: entity.setId(text);
            case TYPE_TAG: entity.setType(text);
            case TXT_TAG: entity.setTxt(text);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        exTag = qName;

        if (ERROR_TAG.equals(exTag)) {
            list.getErrorEntities().add(entity);
            entity = null;
        }

        exTag = null;
    }
}