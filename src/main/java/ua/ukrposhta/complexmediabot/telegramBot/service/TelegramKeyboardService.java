package ua.ukrposhta.complexmediabot.telegramBot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonButton;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonKeyboard;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonRow;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.service.SaxHandlerService;
import ua.ukrposhta.complexmediabot.service.SaxParserService;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;

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
public class TelegramKeyboardService {

    private final BotLogger logger = BotLogger.getLogger(LoggerType.CONSOLE);
    private final BotLogger telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);
    private static final int MAX_BUTTONS_COUNT_IN_ROW = 2;
    private SaxParserService parser;
    private SaxHandlerService handler;

    public TelegramKeyboardService(SaxParserService parser, SaxHandlerService handler) {
        this.parser = parser;
        this.handler = handler;
    }

    public CommonKeyboard getCommonKeyboardReply(BotContext context){

        logger.info("START getCommonKeyboardReply method in TelegramKeyboardService.class");
        telegramLogger.info("START getCommonKeyboardReply method in TelegramKeyboardService.class");

        ButtonEntityList buttonEntityList = getButtonEntityList(context);
        BotState state = BotState.valueOf(context.getTelegramPerson().getCurrentStateName());

        switch (state){
            case END:
                return buildCommonKeyboardReply(new ArrayList<ButtonEntity>(){{
                    add(getCommonButton(ButtonType.START, buttonEntityList));
                }});
            case SELECT:
                return buildCommonKeyboardReply(new ArrayList<ButtonEntity>(){{
                    add(getCommonButton(ButtonType.UA, buttonEntityList));
                    add(getCommonButton(ButtonType.EN, buttonEntityList));
                    add(getCommonButton(ButtonType.END_WORK, buttonEntityList));
                }});

            case GOOD_DAY:
                if(!context.getTelegramPerson().isActivity()) {
                    return buildCommonKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getCommonButton(ButtonType.REQUEST, buttonEntityList));
                        add(getCommonButton(ButtonType.END, buttonEntityList));
                    }});
                } else {
                    return buildCommonKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getCommonButton(ButtonType.REPEAT_REQUEST, buttonEntityList));
                        add(getCommonButton(ButtonType.END, buttonEntityList));
                    }});
                }
            case MEDIA:
                    return buildCommonKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getCommonButton(ButtonType.REPEAT_REQUEST, buttonEntityList));
                        add(getCommonButton(ButtonType.END, buttonEntityList));
                    }});
            default:
                if(context.getTelegramPerson().getIncomTelegramMessage().getLanguageCode().equalsIgnoreCase("unknown")) {
                    return buildCommonKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getCommonButton(ButtonType.END_WORK, buttonEntityList));
                    }});
                }else {
                    return buildCommonKeyboardReply(new ArrayList<ButtonEntity>() {{
                        add(getCommonButton(ButtonType.END, buttonEntityList));
                    }});
                }
        }
    }

    public InlineKeyboardMarkup getCommonKeyboardInline(BotContext context) {

        logger.info("START getCommonKeyboardInline method in TelegramKeyboardService.class");
        telegramLogger.info("START getCommonKeyboardInline method in TelegramKeyboardService.class");

        ButtonEntityList buttonEntityList = getButtonEntityList(context);
        BotState state = BotState.valueOf(context.getTelegramPerson().getCurrentStateName());

        switch (state) {
            case PHONE:
                return buildCommonKeyboardInline(new ArrayList<ButtonEntity>(){{
                    add(getCommonButton(ButtonType.INSERT_NAME, buttonEntityList));
                }}, context);
            default:
                return null;
        }
    }

    private ButtonEntity getCommonButton(ButtonType type, ButtonEntityList buttonEntityList){
        return buttonEntityList.getButtonEntities().stream()
                .filter(btn -> btn.getType().equals(type.name()))
                .collect(Collectors.toList()).get(0);
    }

    private CommonKeyboard buildCommonKeyboardReply(List<ButtonEntity> buttonNameList){
        CommonKeyboard keyboard = new CommonKeyboard();
        fillKeyboardByButtons(buttonNameList, keyboard);
        return keyboard;
    }

    private InlineKeyboardMarkup buildCommonKeyboardInline(List<ButtonEntity> buttonNameList, BotContext context){
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        fillInlineKeyboardByInlineButtons(buttonNameList, keyboard, context);
        return keyboard;
    }

    private void fillKeyboardByButtons(List<ButtonEntity> buttonNameList, CommonKeyboard keyboard) {
        int count = 0;
        while (count < buttonNameList.size()) {
            CommonRow commonRow = new CommonRow();
            while (commonRow.getButtonList().size() < MAX_BUTTONS_COUNT_IN_ROW && buttonNameList.size() != count) {
                ButtonEntity btn = buttonNameList.get(count);
                count++;
                CommonButton button = new CommonButton();
                button.setId(Integer.valueOf(btn.getId()));
                button.setText(btn.getTxt());
                commonRow.addButton(button);
            }
            keyboard.addRow(commonRow);
        }
    }

    private void fillInlineKeyboardByInlineButtons(List<ButtonEntity> buttonNameList,
                                                   InlineKeyboardMarkup inlineKeyboard, BotContext context){

        int count = 0;
        int index = 0;
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
        while (count < buttonNameList.size()) {
            inlineRows.add(new ArrayList<>());
            while (inlineRows.get(index).size() < MAX_BUTTONS_COUNT_IN_ROW && buttonNameList.size() != count) {
                ButtonEntity btn = buttonNameList.get(count);
                InlineKeyboardButton inlineBtn = new InlineKeyboardButton();
                inlineBtn.setText(btn.getTxt() + " " + context.getTelegramPerson().getIncomTelegramMessage().getLastName() + " " +
                        context.getTelegramPerson().getIncomTelegramMessage().getFirstName() + " ?");
                inlineBtn.setCallbackData(context.getTelegramPerson().getIncomTelegramMessage().getLastName() + " " +
                        context.getTelegramPerson().getIncomTelegramMessage().getFirstName());
                inlineRows.get(index).add(inlineBtn);
                count++;
            }
            index++;
        }
        inlineKeyboard.setKeyboard(inlineRows);
    }

    @SuppressWarnings("unchecked")
    private ButtonEntityList getButtonEntityList(BotContext context){
        MySaxHandlerForButtons handlerForButtons = (MySaxHandlerForButtons) handler.getHandler(ParserHandlerType.BUTTON);

        handlerForButtons.setBotType(BotType.TELEGRAM.name());

        return (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
                .getObjectExchangeFromXML(context.getTelegramPerson().getButtonPath(), handlerForButtons);
    }
}
