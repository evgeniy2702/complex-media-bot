package ua.ukrposhta.complexmediabot.service;

import org.springframework.stereotype.Service;
import ua.ukrposhta.complexmediabot.utils.parserXml.AbstractParserXML;
import ua.ukrposhta.complexmediabot.utils.parserXml.MySaxParserForButtons;
import ua.ukrposhta.complexmediabot.utils.parserXml.MySaxParserForErrors;
import ua.ukrposhta.complexmediabot.utils.parserXml.MySaxParserForMessage;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Service. It drive which parser to turn on according type of handling data from xml file.
 */

@Service
public class SaxParserService {

    private MySaxParserForMessage mySaxParserForMessage;
    private MySaxParserForButtons mySaxParserForButtons;
    private MySaxParserForErrors mySaxParserForErrors;

    public SaxParserService(MySaxParserForMessage mySaxParserForMessage,
                            MySaxParserForButtons mySaxParserForButtons,
                            MySaxParserForErrors mySaxParserForErrors) {
        this.mySaxParserForMessage = mySaxParserForMessage;
        this.mySaxParserForButtons = mySaxParserForButtons;
        this.mySaxParserForErrors = mySaxParserForErrors;
    }

    public AbstractParserXML getParser(ParserHandlerType type) {
        AbstractParserXML parser;
        switch (type){
            case MESSAGE:
                parser = mySaxParserForMessage;
                break;
            case BUTTON:
                parser = mySaxParserForButtons;
                break;
            case ERROR:
                parser = mySaxParserForErrors;
                break;
            default:
                throw new IllegalArgumentException("Can't resolve parser type of ParserHandlerType!");
        }
        return parser;
    }
}
