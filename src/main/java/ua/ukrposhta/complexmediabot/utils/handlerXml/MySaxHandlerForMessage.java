package ua.ukrposhta.complexmediabot.utils.handlerXml;

import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntityList;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.util.ArrayList;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Handler. It handle data from message.xml file.
 */

@Component
public class MySaxHandlerForMessage extends AbstractHandlerXml<MessageEntityList, MessageEntity>  {

    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    private final String MESSAGE_TAG = "message";

    public MessageEntityList getList() {
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
        consoleLogger.info("start startDocument method in MySaxHandlerForMessage.class");

        list = new MessageEntityList();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        exTag = qName;
        final String MESSAGES_TAG = "messages";

        switch (exTag){
            case MESSAGES_TAG: {
                list.setMessageEntities(new ArrayList<>());
            }
            case MESSAGE_TAG: entity = new MessageEntity();
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

        if (MESSAGE_TAG.equals(exTag)) {
            list.getMessageEntities().add(entity);
            entity = null;
        }

        exTag = null;
    }
}
