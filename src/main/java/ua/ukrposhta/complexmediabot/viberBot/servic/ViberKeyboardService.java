package ua.ukrposhta.complexmediabot.viberBot.servic;


import org.springframework.stereotype.Service;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.service.SaxHandlerService;
import ua.ukrposhta.complexmediabot.service.SaxParserService;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ViberLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;
import ua.ukrposhta.complexmediabot.viberBot.keyboard.ReplyViberButton;
import ua.ukrposhta.complexmediabot.viberBot.keyboard.ViberKeyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Service. It create keyboard and fill it by buttons according state of user .
 */

@Service
public class ViberKeyboardService {
    private final BotLogger console = BotLogger.getLogger(LoggerType.CONSOLE);
    private final BotLogger viberLogger = ViberLogger.getLogger(LoggerType.VIBER);
    private static final int MAX_BUTTONS_COUNT_IN_ROW = 3;
    private static final int IF_BUTTON_IS_ONE_IN_ROW = 6;
    private SaxParserService parser;
    private SaxHandlerService handler;

    public ViberKeyboardService(SaxParserService parser, SaxHandlerService handler) {
        this.parser = parser;
        this.handler = handler;
    }

    public ViberKeyboard getViberKeyboard(BotContext context){

        console.info("START getViberKeyboard method in ViberKeyboardService.class");
        viberLogger.info("START getViberKeyboard method in ViberKeyboardService.class");

        ButtonEntityList buttonEntityList = getButtonEntityList(context);
        BotState state = BotState.valueOf(context.getViberPerson().getCurrentStateName());

        switch (state){
            case SELECT:
                return buildViberKeyboardReply(new ArrayList<ButtonEntity>(){{
                    add(getViberButtonEntity(ButtonType.UA, buttonEntityList));
                    add(getViberButtonEntity(ButtonType.EN, buttonEntityList));
                    add(getViberButtonEntity(ButtonType.END_WORK, buttonEntityList));
                }});
            case END:
                return buildViberKeyboardReply(new ArrayList<ButtonEntity>(){{
                    add(getViberButtonEntity(ButtonType.START, buttonEntityList));
                }});
            case MEDIA:
                return buildViberKeyboardReply(new ArrayList<ButtonEntity>() {{
                    add(getViberButtonEntity(ButtonType.REPEAT_REQUEST, buttonEntityList));
                    add(getViberButtonEntity(ButtonType.END, buttonEntityList));
                }});
            case GOOD_DAY:
                if(!context.getViberPerson().isActivity()) {
                    return buildViberKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getViberButtonEntity(ButtonType.REQUEST, buttonEntityList));
                        add(getViberButtonEntity(ButtonType.END, buttonEntityList));
                    }});
                } else {
                    return buildViberKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getViberButtonEntity(ButtonType.REPEAT_REQUEST, buttonEntityList));
                        add(getViberButtonEntity(ButtonType.END, buttonEntityList));
                    }});
                }
            default:
                if(context.getViberPerson().getViberSender().getLanguage().equalsIgnoreCase("unknown")) {
                    return buildViberKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getViberButtonEntity(ButtonType.END_WORK, buttonEntityList));
                    }});
                }else {
                    return buildViberKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getViberButtonEntity(ButtonType.END, buttonEntityList));
                    }});
                }
        }
    }

    private ButtonEntity getViberButtonEntity(ButtonType type, ButtonEntityList buttonEntityList){
        return buttonEntityList.getButtonEntities().stream()
                .filter(btn -> btn.getType().equals(type.name()))
                .collect(Collectors.toList()).get(0);
    }

    private ViberKeyboard buildViberKeyboardReply(List<ButtonEntity> buttonNameList){
        ViberKeyboard keyboard = new ViberKeyboard();
        fillKeyboardByButtons(buttonNameList, keyboard);
        return keyboard;
    }

    private void fillKeyboardByButtons(List<ButtonEntity> buttonNameList, ViberKeyboard keyboard) {

        int count = 0;
        while (count < buttonNameList.size()) {

            ButtonEntity btn = buttonNameList.get(count);
            ReplyViberButton button;

            if(buttonNameList.size()%2 != 0 && (buttonNameList.size() - count == 1)){
                button = new ReplyViberButton(IF_BUTTON_IS_ONE_IN_ROW,1,btn.getTxt(),btn.getTxt());
            } else{
                button = new ReplyViberButton(MAX_BUTTONS_COUNT_IN_ROW,1,btn.getTxt(),btn.getTxt());
            }

            count++;

            keyboard.addButton(button);
        }

    }

    @SuppressWarnings("unchecked")
    private ButtonEntityList getButtonEntityList(BotContext context){
        MySaxHandlerForButtons handlerForButtons = (MySaxHandlerForButtons) handler.getHandler(ParserHandlerType.BUTTON);

        handlerForButtons.setBotType(BotType.VIBER.name());

        return (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
                .getObjectExchangeFromXML(context.getViberPerson().getButtonPath(), handlerForButtons);
    }
}