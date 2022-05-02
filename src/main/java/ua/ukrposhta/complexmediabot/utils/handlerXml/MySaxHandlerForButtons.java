package ua.ukrposhta.complexmediabot.utils.handlerXml;

import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.util.ArrayList;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Handler. It handle data from buttons.xml file.
 */

@Component
public class MySaxHandlerForButtons extends AbstractHandlerXml<ButtonEntityList, ButtonEntity> {

    private final String BUTTON_TAG = "button";

    public void setBotType(String botType) {
        this.botType = botType;
    }

    public ButtonEntityList getList() {
        return list;
    }

    public BotLogger getLogger(String botType){
        return BotLogger.getLogger(LoggerType.valueOf(botType));
    }

    @Override
    public void startDocument() throws SAXException {
        consoleLogger.info("----------------------------------------------------------");
        consoleLogger.info("start startDocument method in MySaxHandlerForButtons.class");

        list = new ButtonEntityList();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        exTag = qName;

        final String BUTTONS_TAG = "buttons";

        switch (exTag){
            case BUTTONS_TAG: {
                list.setButtonEntities(new ArrayList<>());
            }
            case BUTTON_TAG: entity = new ButtonEntity();
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

        if (BUTTON_TAG.equals(exTag)) {
            list.getButtonEntities().add(entity);
            entity = null;
        }

        exTag = null;
    }
}

