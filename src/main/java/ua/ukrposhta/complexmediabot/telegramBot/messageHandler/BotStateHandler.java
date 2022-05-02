package ua.ukrposhta.complexmediabot.telegramBot.messageHandler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.telegramBot.message.OutputMessage;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.Person;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.AbstractEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntityList;
import ua.ukrposhta.complexmediabot.telegramBot.service.KeyboardService;
import ua.ukrposhta.complexmediabot.service.SaxHandlerService;
import ua.ukrposhta.complexmediabot.service.SaxParserService;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForErrors;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForMessage;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Handler. It handle a state of user and create output message for sending it to telegram bot
 */

@Component
public class BotStateHandler {

    private SaxHandlerService handler;
    private SaxParserService parser;
    private KeyboardService keyboard;

    private BotLogger telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);
    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    public BotStateHandler(SaxHandlerService handler,
                           SaxParserService parser,
                           KeyboardService keyboard) {
        this.handler = handler;
        this.parser = parser;
        this.keyboard = keyboard;
    }

//    Обработчик состояния пользователя
    @SuppressWarnings("unchecked")
    public BotState handleIncomingMessage(Update update, BotContext context, TelegramBot bot){

        consoleLogger.info("START handleIncomingMessage method in BotStateHandler.class");
        telegramLogger.info("START handleIncomingMessage method in BotStateHandler.class");

        MySaxHandlerForMessage handlerForMessage = (MySaxHandlerForMessage) handler.getHandler(ParserHandlerType.MESSAGE);
        MySaxHandlerForErrors handlerForErrors = (MySaxHandlerForErrors) handler.getHandler(ParserHandlerType.ERROR);
        MySaxHandlerForButtons handlerForButtons = (MySaxHandlerForButtons) handler.getHandler(ParserHandlerType.BUTTON);

        handlerForErrors.setBotType(BotType.TELEGRAM.name());
        handlerForMessage.setBotType(BotType.TELEGRAM.name());
        handlerForButtons.setBotType(BotType.TELEGRAM.name());

        MessageEntityList messageEntityList = (MessageEntityList) parser.getParser(ParserHandlerType.MESSAGE)
                .getObjectExchangeFromXML(context.getPerson().getMessagePath(),handlerForMessage);

        ErrorEntityList errorEntityList = (ErrorEntityList) parser.getParser(ParserHandlerType.ERROR)
                .getObjectExchangeFromXML(context.getPerson().getErrorPath(), handlerForErrors);

        ButtonEntityList buttonEntityList = (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
        .getObjectExchangeFromXML(context.getPerson().getButtonPath(), handlerForButtons);

        Pattern pattern;
        Matcher matcher;
        MessageEntity messageEntity;
        ErrorEntity errorEntity;
        OutputMessage outputMessage;
        BotState state = BotState.valueOf(context.getPerson().getCurrentStateName());

        switch (state){
            case GOOD_DAY:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.GOOD_DAY.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                messageEntity = getMessageByCurrentStateMinusOne(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                messageEntity = getMessageByCurrentStatePlusTwo(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case LANGUAGE:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.LANGUAGE.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                messageEntity = getMessageByCurrentStatePlusTwo(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case SELECT:

                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.SELECT.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                handlerOfError(messageEntityList, errorEntityList, buttonEntityList, keyboard, context, bot);

                break;
            case MEDIA:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.MEDIA.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                handlerOfError(messageEntityList, errorEntityList, buttonEntityList, keyboard, context, bot);
                break;

            case NAME_SURNAME:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.NAME_SURNAME.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                context.getPerson().setMediaName(update.getMessage().getText());

                messageEntity = getMessageByCurrentState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.sendInlineKeyboard(outputMessage);
                break;

            case PHONE:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.PHONE.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                if(!hasCallbackDataOnName_Surname(update, context)){
                    context.getPerson().setName_surname(update.getMessage().getText());

                    messageEntity = getMessageByCurrentState(messageEntityList, context);
                    outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                    bot.send(outputMessage);
                } else {

                    messageEntity = getMessageByCurrentState(messageEntityList, context);
                    outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                    bot.send(outputMessage);
                }
                break;
            case EMAIL:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.EMAIL.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                pattern = Pattern.compile("^\\+380[0-9]{2} [0-9]{3} [0-9]{2} [0-9]{2}$");
                matcher = pattern.matcher(update.getMessage().getText());
                if(matcher.find()){
                    String phoneFormater = " " + update.getMessage().getText().split(" ")[0].substring(0,3) + "(" +
                            update.getMessage().getText().split(" ")[0].substring(3,6) +") " +
                            update.getMessage().getText().split(" ")[1] + " " +
                            update.getMessage().getText().split(" ")[2] + " " +
                            update.getMessage().getText().split(" ")[3];
                    context.getPerson().setPhone(phoneFormater);

                    messageEntity = getMessageByCurrentState(messageEntityList, context);
                    outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                    bot.send(outputMessage);
                } else {
                    errorEntity = getErrorMessageByStateName(errorEntityList, context);
                    outputMessage = makeOutputMessage(errorEntity, keyboard, context);

                    bot.send(outputMessage);
                }
                break;
            case SUBJECT:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.SUBJECT.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                pattern = Pattern.compile("^([A-Za-z0-9_-]+\\.)*[A-Za-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,3}$");
                matcher = pattern.matcher(update.getMessage().getText());
                if(matcher.find() ) {
                    context.getPerson().setEmail(update.getMessage().getText());

                    messageEntity = getMessageByCurrentState(messageEntityList, context);
                    outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                    bot.send(outputMessage);

                } else {
                    errorEntity = getErrorMessageByStateName(errorEntityList, context);
                    outputMessage = makeOutputMessage(errorEntity, keyboard, context);

                    bot.send(outputMessage);
                }
                break;

            case WE_CONTACT:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.WE_CONTACT.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                context.getPerson().setSubject(update.getMessage().getText());
                messageEntity = getMessageByCurrentState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                if(context.getPerson().isExit())
                    context.getPerson().setExit(false);

                bot.send(outputMessage);
                break;

            case END:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.END.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getPerson().getPrevStateName());

                if (context.getPerson().isExit()) {
                    context.getPerson().setActivity(false);
                } else {
                    context.getPerson().setActivity(true);
                }
                messageEntity = getMessageForEndState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                switchLanguage(context.getPerson());
                break;
        }

        return BotState.valueOf(context.getPerson().getCurrentStateName());
    }

//   Формирование исходящего JSON объекта для телеграмм бота в соответствии с состояниями пользователя
    private OutputMessage makeOutputMessage(AbstractEntity abstractEntity, KeyboardService keyboardService, BotContext context) {
        consoleLogger.info("START makeOutputMessage method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class makeOutputMessage method CurrentState of person : " + context.getPerson().getCurrentStateName());
        BotState state = BotState.valueOf(context.getPerson().getCurrentStateName());
        switch (state){
            case PHONE:
                return OutputMessage.builder()
                        .chat_id(context.getPerson().getIncomingTelegramMessage().getChat_id())
                        .context(context)
                        .message_text(abstractEntity.getTxt())
                        .inlineKeyboardMarkup(keyboardService.getCommonKeyboardInline(context))
                        .build();

             default:
                 return OutputMessage.builder()
                                 .chat_id(context.getPerson().getIncomingTelegramMessage().getChat_id())
                                 .context(context)
                                 .message_text(abstractEntity.getTxt())
                                 .replyKeyboardReply(keyboardService.getCommonKeyboardReply(context))
                                 .build();
        }
    }

//    Формирует сообщение пользователю согласно его текущему состоянию с переводом текущего состояния на +1 позицию
//    вперед и соответсвенно ему присвоение предыдущего состояния пользователю
    private MessageEntity getMessageByCurrentState ( MessageEntityList messageEntityList, BotContext context){
        consoleLogger.info("START getMessageByCurrentState method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class getMessageByCurrentState method CurrentState of person : " +
                context.getPerson().getCurrentStateName());
        MessageEntity messageEntity = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getPerson().getCurrentStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        context.getPerson().setPrevStateName(context.getPerson().getCurrentStateName());
        int middleStateInt = BotState.valueOf(context.getPerson().getCurrentStateName()).ordinal() + 1;
        context.getPerson().setCurrentStateName(BotState.byId(middleStateInt).name());
        return messageEntity;
    }

    //    Формирует сообщение пользователю с InlineKeyboard согласно его текущему состоянию с переводом текущего
    //    состояния на +1 позицию вперед и в соответсвии ему присвоение предыдущего состояния пользователю
    private MessageEntity getMessageByCurrentStateForInlineKeyboard ( MessageEntityList messageEntityList, BotContext context){
        consoleLogger.info("START getMessageByCurrentStateForInlineKeyboard method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class getMessageByCurrentStateForInlineKeyboard method CurrentState of person : " +
                context.getPerson().getCurrentStateName());
        return messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getPerson().getCurrentStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
    }

//    Изменяет текущее и предыдущее состояния пользователя для корректной работы InlineKeyboardMarkup
    private void changeStateOfPerson(BotContext context){
        consoleLogger.info("START changeStateOfPerson method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class changeStateOfPerson method CurrentState of person : " +
                context.getPerson().getCurrentStateName());
        int middleStateInt = BotState.valueOf(context.getPerson().getCurrentStateName()).ordinal() + 1;
        context.getPerson().setPrevStateName(context.getPerson().getCurrentStateName());
        context.getPerson().setCurrentStateName(BotState.byId(middleStateInt).name());
    }

//    Формирует сообщение пользователю согласно его текущему состоянию соответствующему BotState.END
    private MessageEntity getMessageForEndState ( MessageEntityList messageEntityList, BotContext context){
        consoleLogger.info("START getMessageForEndState method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class getMessageForEndState method CurrentState of person : " +
                context.getPerson().getCurrentStateName());
        return messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getPerson().getCurrentStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
    }

//    Формирует сообщение об ошибке на основании предыдущего сосотояния пользователя с приведением текущее и
//    предыдущее состояние к предыдущим значениям
    private ErrorEntity getErrorMessageByStateName(ErrorEntityList errorEntityList, BotContext context){
        consoleLogger.info("START getErrorMessageByStateName method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class getErrorMessageByStateName method PrevState of person : " +
                context.getPerson().getPrevStateName());
        return errorEntityList.getErrorEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
    }

//  Проверяет состояние пользователя на соответствие BotState.ERROR
    private boolean hasErrorInInput(BotContext context){
        for (BotState stateItem : BotState.values() ) {
            if( context.getInput().equals(stateItem.getName()) ||
                    context.getInput().equals(ButtonType.UA.getText()) ||
                    context.getInput().equals(ButtonType.EN.getText()) ) {
                consoleLogger.info("FALSE in hasErrorInInput method of BotStateHandler.class");
                telegramLogger.info("FALSE in hasErrorInInput method of BotStateHandler.class");
                return false;
            }
        }
        consoleLogger.info("TRUE in hasErrorInInput method of BotStateHandler.class");
        telegramLogger.info("TRUE in hasErrorInInput method of BotStateHandler.class");
        return true;
    }

    private boolean hasErrorInButton(BotContext context, ButtonEntityList buttonEntityList){
        for (ButtonEntity button : buttonEntityList.getButtonEntities() ) {
            if( context.getInput().equals(button.getTxt())){
                consoleLogger.info("FALSE in hasErrorInButton method of BotStateHandler.class");
                telegramLogger.info("FALSE in hasErrorInButton method of BotStateHandler.class");
                return false;
            }
        }
        consoleLogger.info("TRUE in hasErrorInButton method of BotStateHandler.class");
        telegramLogger.info("TRUE in hasErrorInButton method of BotStateHandler.class");
        return true;
    }

//    Обработчик , который при присутсвии ошибок формирует и отправляет пользователю сообщение об ошибке
//    либо формирует сообщение в соответствии с отправленным от него выбором
    private void handlerOfError(MessageEntityList messageEntityList, ErrorEntityList errorEntityList,
                                ButtonEntityList buttonEntityList, KeyboardService keyboardService,
                                BotContext context, TelegramBot bot){
        consoleLogger.info("START handlerOfError method of BotStateHandler.class");

        if(!hasErrorInInput(context) || !hasErrorInButton(context, buttonEntityList)){
            telegramLogger.info("THERE IS NO errors in handlerOfError method of BotStateHandler.class");
            MessageEntity messageEntity = getMessageByCurrentState(messageEntityList, context);
            OutputMessage outputMessage = makeOutputMessage(messageEntity,keyboardService, context);
            bot.send(outputMessage);
        } else {
            telegramLogger.info("THERE IS errors in handlerOfError method of BotStateHandler.class");
            ErrorEntity errorEntity = getErrorMessageByStateNameEqualsERROR(errorEntityList);
            OutputMessage outputMessage = makeOutputMessage(errorEntity, keyboardService, context);
            bot.send(outputMessage);
        }

    }

//    Выбор сообщения об ошибке (PHONE, EMAIL, ERROR) в соответствии с текущим состоянием пользователя
    private ErrorEntity getErrorMessageByStateNameEqualsERROR(ErrorEntityList errorEntityList){
        consoleLogger.info("START getErrorMessageByStateNameEqualsERROR method of BotStateHandler.class");
        telegramLogger.info("ERROR : " + BotState.ERROR.name() +
                " in getErrorMessageByStateNameEqualsERROR method of BotStateHandler.class");
        return errorEntityList.getErrorEntities().stream()
                .filter(err -> err.getType()
                        .equals(BotState.ERROR.name()))
                .collect(Collectors.toList())
                .get(0);
    }

//    Отработает в случае, если пользоватлеь решит воспользоваться имеющимися данными о его фамилии и имени
//    в телеграмм
    private boolean hasCallbackDataOnName_Surname(Update update, BotContext context){
        if(update.hasCallbackQuery()) {
            context.getPerson()
                    .setName_surname(update.getCallbackQuery().getData());
            consoleLogger.info("TRUE in hasCallbackDataOnName_Surname method of BotStateHandler.class");
            telegramLogger.info("TRUE in hasCallbackDataOnName_Surname method of BotStateHandler.class");
            return true;
        } else {
            consoleLogger.info("FALSE in hasCallbackDataOnName_Surname method of BotStateHandler.class");
            telegramLogger.info("FALSE in hasCallbackDataOnName_Surname method of BotStateHandler.class");
            return false;
        }
    }
//      Устанавливает текущее и предыдущее состояния пользователя в соответствии с имеющимся у пользователя
//      language_code
    private void switchLanguage( Person person) {
        consoleLogger.info("START switchLanguage method of BotStateHandler.class");
        switch (person.getIncomingTelegramMessage().getLanguageCode()){
            case "uk":
                person.setCurrentStateName(BotState.GOOD_DAY.name());
                person.setPrevStateName(BotState.START.name());
                person.setAddDate(LocalDateTime.now());
                break;
            case "en":
                person.setCurrentStateName(BotState.GOOD_DAY.name());
                person.setPrevStateName(BotState.START.name());
                person.setAddDate(LocalDateTime.now());
                break;
            default:
                person.getIncomingTelegramMessage().setLanguageCode("unknown");
                person.setCurrentStateName(BotState.LANGUAGE.name());
                person.setPrevStateName(BotState.LANGUAGE.name());
                break;
        }
        telegramLogger.info("BotStateHandler.class LanguageCode of person is : " +
                person.getIncomingTelegramMessage().getLanguageCode());
    }

//    Получение тела сообщения пользователю по его предыдущему состоянию с присвоенеим в последующем пользователю
//    текущего сосотояния на 2 позиии вперед и в соответствии текущему присвоить значение предыдущего состояния
    private MessageEntity getMessageByCurrentStatePlusTwo ( MessageEntityList messageEntityList, BotContext context){

        consoleLogger.info("START getMessageByCurrentStatePlusTwo method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class getMessageByCurrentStatePlusTwo method PrevState of person : " +
                context.getPerson().getPrevStateName());

        MessageEntity messageEntity = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        int middleStateInt = BotState.valueOf(context.getPerson().getCurrentStateName()).ordinal() + 2;
        context.getPerson().setPrevStateName(BotState.byId(middleStateInt - 1).name());
        context.getPerson().setCurrentStateName(BotState.byId(middleStateInt).name());
        return messageEntity;
    }

//    Получение тела сообщения пользователю по его предыдущему состоянию с присвоенеим в последующем пользователю
//    текущего сосотояния на 1 позиии назад и в соответствии текущему присвоить значение предыдущего состояния
    private MessageEntity getMessageByCurrentStateMinusOne (MessageEntityList messageEntityList, BotContext context){

        consoleLogger.info("START getMessageByCurrentStateMinusOne method of BotStateHandler.class");
        telegramLogger.info("BotStateHandler.class getMessageByCurrentStateMinusOne method PrevState of person : " +
                context.getPerson().getPrevStateName());

        MessageEntity messageEntity =  messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        context.getPerson().setPrevStateName(context.getPerson().getCurrentStateName());
        return messageEntity;
    }
}
