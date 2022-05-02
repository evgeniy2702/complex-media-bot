package ua.ukrposhta.complexmediabot.utils.parserXml;

import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntityList;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForMessage;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Parser. It parse data from message.xml file.
 */

@Component
public class MySaxParserForMessage extends AbstractParserXML<MessageEntityList, MySaxHandlerForMessage>{

}
