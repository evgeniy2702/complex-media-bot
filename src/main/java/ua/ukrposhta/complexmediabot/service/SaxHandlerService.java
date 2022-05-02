package ua.ukrposhta.complexmediabot.service;

import org.springframework.stereotype.Service;
import ua.ukrposhta.complexmediabot.utils.handlerXml.AbstractHandlerXml;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForErrors;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForMessage;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Service. It drive which handler to turn on according type of handling data from xml file.
 */


@Service
public class  SaxHandlerService {

    private MySaxHandlerForMessage mySaxHandlerForMessage;
    private MySaxHandlerForButtons mySaxHandlerForButtons;
    private MySaxHandlerForErrors mySaxHandlerForErrors;

    public SaxHandlerService(MySaxHandlerForMessage mySaxHandlerForMessage,
                             MySaxHandlerForButtons mySaxHandlerForButtons,
                             MySaxHandlerForErrors mySaxHandlerForErrors) {
        this.mySaxHandlerForMessage = mySaxHandlerForMessage;
        this.mySaxHandlerForButtons = mySaxHandlerForButtons;
        this.mySaxHandlerForErrors = mySaxHandlerForErrors;
    }



    public AbstractHandlerXml getHandler(ParserHandlerType type){
        AbstractHandlerXml handler;
        switch (type){
            case MESSAGE:
                handler = mySaxHandlerForMessage;
                break;
            case BUTTON:
                handler = mySaxHandlerForButtons;
                break;
            case ERROR:
                handler = mySaxHandlerForErrors;
                break;
            default:
                throw new IllegalArgumentException("Can't resolve handler type of ParserHandlerType!");
        }
        return handler;
    }

}
