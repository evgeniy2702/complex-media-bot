package ua.ukrposhta.complexmediabot.utils.parserXml;

import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntityList;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForErrors;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Parser. It parse data from errors.xml file.
 */

@Component
public class MySaxParserForErrors extends AbstractParserXML<ErrorEntityList, MySaxHandlerForErrors> {

}
