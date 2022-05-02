package ua.ukrposhta.complexmediabot.utils.parserXml;

import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Parser. It parse data from buttons.xml file.
 */

@Component
public class MySaxParserForButtons extends AbstractParserXML<ButtonEntityList, MySaxHandlerForButtons> {

}
