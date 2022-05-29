package ua.ukrposhta.complexmediabot.telegramBot.messageHandler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.AbstractEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntityList;
import ua.ukrposhta.complexmediabot.service.SaxHandlerService;
import ua.ukrposhta.complexmediabot.service.SaxParserService;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.model.OutputMessage;
import ua.ukrposhta.complexmediabot.telegramBot.service.TelegramKeyboardService;
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
public class TelegramBotStateHandler {

    private SaxHandlerService handler;
    private SaxParserService parser;
    private TelegramKeyboardService keyboard;

    private BotLogger telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);
    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    public TelegramBotStateHandler(SaxHandlerService handler,
                           SaxParserService parser,
                           TelegramKeyboardService keyboard) {
        this.handler = handler;
        this.parser = parser;
        this.keyboard = keyboard;
    }

//    Обработчик состояния пользователя
    @SuppressWarnings("unchecked")
    public BotState handleIncomingMessage(Update update, BotContext context, TelegramBot bot){

        consoleLogger.info("START handleIncomingMessage method in TelegramBotStateHandler.class");
        telegramLogger.info("START handleIncomingMessage method in TelegramBotStateHandler.class");

        MySaxHandlerForMessage handlerForMessage = (MySaxHandlerForMessage) handler.getHandler(ParserHandlerType.MESSAGE);
        MySaxHandlerForErrors handlerForErrors = (MySaxHandlerForErrors) handler.getHandler(ParserHandlerType.ERROR);
        MySaxHandlerForButtons handlerForButtons = (MySaxHandlerForButtons) handler.getHandler(ParserHandlerType.BUTTON);

        handlerForErrors.setBotType(BotType.TELEGRAM.name());
        handlerForMessage.setBotType(BotType.TELEGRAM.name());
        handlerForButtons.setBotType(BotType.TELEGRAM.name());

        MessageEntityList messageEntityList = (MessageEntityList) parser.getParser(ParserHandlerType.MESSAGE)
                .getObjectExchangeFromXML(context.getTelegramPerson().getMessagePath(),handlerForMessage);

        ErrorEntityList errorEntityList = (ErrorEntityList) parser.getParser(ParserHandlerType.ERROR)
                .getObjectExchangeFromXML(context.getTelegramPerson().getErrorPath(), handlerForErrors);

        ButtonEntityList buttonEntityList = (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
        .getObjectExchangeFromXML(context.getTelegramPerson().getButtonPath(), handlerForButtons);

        Pattern pattern;
        Matcher matcher;
        MessageEntity messageEntity;
        ErrorEntity errorEntity;
        OutputMessage outputMessage;
        BotState state = BotState.valueOf(context.getTelegramPerson().getCurrentStateName());

        switch (state){
            case GOOD_DAY:
                telegramLogger.info("BotStateHandler.class CurrentState is : " + BotState.GOOD_DAY.name());
                telegramLogger.info("BotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("BotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                messageEntity = getMessageByCurrentStateMinusOne(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                messageEntity = getMessageByCurrentStatePlusTwo(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case LANGUAGE:
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.LANGUAGE.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                messageEntity = getMessageByCurrentStatePlusTwo(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case SELECT:
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.SELECT.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                handlerOfError(messageEntityList, errorEntityList, buttonEntityList, keyboard, context, bot);

                break;
            case MEDIA:
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.MEDIA.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                handlerOfError(messageEntityList, errorEntityList, buttonEntityList, keyboard, context, bot);
                break;

            case NAME_SURNAME:
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.NAME_SURNAME.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                context.getTelegramPerson().setMediaName(update.getMessage().getText());

                messageEntity = getMessageByCurrentState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.sendInlineKeyboard(outputMessage);
                break;

            case PHONE:
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.PHONE.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                if(!hasCallbackDataOnName_Surname(update, context)){
                    context.getTelegramPerson().setName_surname(update.getMessage().getText());

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
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.EMAIL.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                pattern = Pattern.compile("^\\+380[0-9]{2} [0-9]{3} [0-9]{2} [0-9]{2}$");
                matcher = pattern.matcher(update.getMessage().getText());
                if(matcher.find()){
                    String phoneFormater = " " + update.getMessage().getText().split(" ")[0].substring(0,3) + "(" +
                            update.getMessage().getText().split(" ")[0].substring(3,6) +") " +
                            update.getMessage().getText().split(" ")[1] + " " +
                            update.getMessage().getText().split(" ")[2] + " " +
                            update.getMessage().getText().split(" ")[3];
                    context.getTelegramPerson().setPhone(phoneFormater);

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
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.SUBJECT.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                pattern = Pattern.compile("^([A-Za-z0-9_-]+\\.)*[A-Za-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,3}$");
                matcher = pattern.matcher(update.getMessage().getText());
                if(matcher.find() ) {
                    context.getTelegramPerson().setEmail(update.getMessage().getText());

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
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.WE_CONTACT.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                context.getTelegramPerson().setSubject(update.getMessage().getText());
                messageEntity = getMessageByCurrentState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                if(context.getTelegramPerson().isExit())
                    context.getTelegramPerson().setExit(false);

                bot.send(outputMessage);
                break;

            case END:
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + BotState.END.name());
                telegramLogger.info("TelegramBotStateHandler.class CurrentState is : " + context.getTelegramPerson().getCurrentStateName());
                telegramLogger.info("TelegramBotStateHandler.class PrevState is : " + context.getTelegramPerson().getPrevStateName());

                if (context.getTelegramPerson().isExit()) {
                    context.getTelegramPerson().setActivity(false);
                } else {
                    context.getTelegramPerson().setActivity(true);
                }
                messageEntity = getMessageForEndState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                switchLanguage(context.getTelegramPerson());
                break;
        }

        return BotState.valueOf(context.getTelegramPerson().getCurrentStateName());
    }

//   Формирование исходящего JSON объекта для телеграмм бота в соответствии с состояниями пользователя
    private OutputMessage makeOutputMessage(AbstractEntity abstractEntity, TelegramKeyboardService keyboardService, BotContext context) {
        consoleLogger.info("START makeOutputMessage method of TelegramBotStateHandler.class");
        telegramLogger.info("TelegramBotStateHandler.class makeOutputMessage method CurrentState of telegramPerson : " + context.getTelegramPerson().getCurrentStateName());
        BotState state = BotState.valueOf(context.getTelegramPerson().getCurrentStateName());
        switch (state){
            case PHONE:
                return OutputMessage.builder()
                        .chat_id(context.getTelegramPerson().getIncomTelegramMessage().getChat_id())
                        .context(context)
                        .message_text(abstractEntity.getTxt())
                        .inlineKeyboardMarkup(keyboardService.getCommonKeyboardInline(context))
                        .build();

             default:
                 return OutputMessage.builder()
                                 .chat_id(context.getTelegramPerson().getIncomTelegramMessage().getChat_id())
                                 .context(context)
                                 .message_text(abstractEntity.getTxt())
                                 .replyKeyboardReply(keyboardService.getCommonKeyboardReply(context))
                                 .build();
        }
    }

//    Формирует сообщение пользователю согласно его текущему состоянию с переводом текущего состояния на +1 позицию
//    вперед и соответсвенно ему присвоение предыдущего состояния пользователю
    private MessageEntity getMessageByCurrentState ( MessageEntityList messageEntityList, BotContext context){
        consoleLogger.info("START getMessageByCurrentState method of TelegramBotStateHandler.class");
        telegramLogger.info("TelegramBotStateHandler.class getMessageByCurrentState method CurrentState of telegramPerson : " +
                context.getTelegramPerson().getCurrentStateName());
        MessageEntity messageEntity = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getTelegramPerson().getCurrentStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        context.getTelegramPerson().setPrevStateName(context.getTelegramPerson().getCurrentStateName());
        int middleStateInt = BotState.valueOf(context.getTelegramPerson().getCurrentStateName()).ordinal() + 1;
        context.getTelegramPerson().setCurrentStateName(BotState.byId(middleStateInt).name());
        return messageEntity;
    }

//    Формирует сообщение пользователю согласно его текущему состоянию соответствующему BotState.END
    private MessageEntity getMessageForEndState ( MessageEntityList messageEntityList, BotContext context){
        consoleLogger.info("START getMessageForEndState method of TelegramBotStateHandler.class");
        telegramLogger.info("TelegramBotStateHandler.class getMessageForEndState method CurrentState of telegramPerson : " +
                context.getTelegramPerson().getCurrentStateName());
        return messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getTelegramPerson().getCurrentStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
    }

//    Формирует сообщение об ошибке на основании предыдущего сосотояния пользователя с приведением текущее и
//    предыдущее состояние к предыдущим значениям
    private ErrorEntity getErrorMessageByStateName(ErrorEntityList errorEntityList, BotContext context){
        consoleLogger.info("START getErrorMessageByStateName method of TelegramBotStateHandler.class");
        telegramLogger.info("TelegramBotStateHandler.class getErrorMessageByStateName method PrevState of telegramPerson : " +
                context.getTelegramPerson().getPrevStateName());
        return errorEntityList.getErrorEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getTelegramPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
    }

//  Проверяет состояние пользователя на соответствие BotState.ERROR
    private boolean hasErrorInInput(BotContext context){
        for (BotState stateItem : BotState.values() ) {
            if( context.getInput().equals(stateItem.getName()) ||
                    context.getInput().equals(ButtonType.UA.getText()) ||
                    context.getInput().equals(ButtonType.EN.getText()) ) {
                consoleLogger.info("FALSE in hasErrorInInput method of TelegramBotStateHandler.class");
                telegramLogger.info("FALSE in hasErrorInInput method of TelegramBotStateHandler.class");
                return false;
            }
        }
        consoleLogger.info("TRUE in hasErrorInInput method of TelegramBotStateHandler.class");
        telegramLogger.info("TRUE in hasErrorInInput method of TelegramBotStateHandler.class");
        return true;
    }

    private boolean hasErrorInButton(BotContext context, ButtonEntityList buttonEntityList){
        for (ButtonEntity button : buttonEntityList.getButtonEntities() ) {
            if( context.getInput().equals(button.getTxt())){
                consoleLogger.info("FALSE in hasErrorInButton method of TelegramBotStateHandler.class");
                telegramLogger.info("FALSE in hasErrorInButton method of TelegramBotStateHandler.class");
                return false;
            }
        }
        consoleLogger.info("TRUE in hasErrorInButton method of TelegramBotStateHandler.class");
        telegramLogger.info("TRUE in hasErrorInButton method of TelegramBotStateHandler.class");
        return true;
    }

//    Обработчик , который при присутсвии ошибок формирует и отправляет пользователю сообщение об ошибке
//    либо формирует сообщение в соответствии с отправленным от него выбором
    private void handlerOfError(MessageEntityList messageEntityList, ErrorEntityList errorEntityList,
                                ButtonEntityList buttonEntityList, TelegramKeyboardService keyboardService,
                                BotContext context, TelegramBot bot){
        consoleLogger.info("START handlerOfError method of TelegramBotStateHandler.class");

        if(!hasErrorInInput(context) || !hasErrorInButton(context, buttonEntityList)){
            telegramLogger.info("THERE IS NO errors in handlerOfError method of TelegramBotStateHandler.class");
            MessageEntity messageEntity = getMessageByCurrentState(messageEntityList, context);
            OutputMessage outputMessage = makeOutputMessage(messageEntity,keyboardService, context);
            bot.send(outputMessage);
        } else {
            telegramLogger.info("THERE IS errors in handlerOfError method of TelegramBotStateHandler.class");
            ErrorEntity errorEntity = getErrorMessageByStateNameEqualsERROR(errorEntityList);
            OutputMessage outputMessage = makeOutputMessage(errorEntity, keyboardService, context);
            bot.send(outputMessage);
        }

    }

//    Выбор сообщения об ошибке (PHONE, EMAIL, ERROR) в соответствии с текущим состоянием пользователя
    private ErrorEntity getErrorMessageByStateNameEqualsERROR(ErrorEntityList errorEntityList){
        consoleLogger.info("START getErrorMessageByStateNameEqualsERROR method of TelegramBotStateHandler.class");
        telegramLogger.info("ERROR : " + BotState.ERROR.name() +
                " in getErrorMessageByStateNameEqualsERROR method of TelegramBotStateHandler.class");
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
            context.getTelegramPerson()
                    .setName_surname(update.getCallbackQuery().getData());
            consoleLogger.info("TRUE in hasCallbackDataOnName_Surname method of TelegramBotStateHandler.class");
            telegramLogger.info("TRUE in hasCallbackDataOnName_Surname method of TelegramBotStateHandler.class");
            return true;
        } else {
            consoleLogger.info("FALSE in hasCallbackDataOnName_Surname method of TelegramBotStateHandler.class");
            telegramLogger.info("FALSE in hasCallbackDataOnName_Surname method of TelegramBotStateHandler.class");
            return false;
        }
    }
//      Устанавливает текущее и предыдущее состояния пользователя в соответствии с имеющимся у пользователя
//      language_code
    private void switchLanguage( TelegramPersonEntity telegramPerson) {
        consoleLogger.info("START switchLanguage method of TelegramBotStateHandler.class");
        switch (telegramPerson.getIncomTelegramMessage().getLanguageCode()){
            case "uk":
                telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                telegramPerson.setPrevStateName(BotState.START.name());
                telegramPerson.setAddDate(LocalDateTime.now());
                break;
            case "en":
                telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                telegramPerson.setPrevStateName(BotState.START.name());
                telegramPerson.setAddDate(LocalDateTime.now());
                break;
            default:
                telegramPerson.getIncomTelegramMessage().setLanguageCode("unknown");
                telegramPerson.setCurrentStateName(BotState.LANGUAGE.name());
                telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
                break;
        }
        telegramLogger.info("TelegramBotStateHandler.class LanguageCode of telegramPerson is : " +
                telegramPerson.getIncomTelegramMessage().getLanguageCode());
    }

//    Получение тела сообщения пользователю по его предыдущему состоянию с присвоенеим в последующем пользователю
//    текущего сосотояния на 2 позиии вперед и в соответствии текущему присвоить значение предыдущего состояния
    private MessageEntity getMessageByCurrentStatePlusTwo ( MessageEntityList messageEntityList, BotContext context){

        consoleLogger.info("START getMessageByCurrentStatePlusTwo method of TelegramBotStateHandler.class");
        telegramLogger.info("TelegramBotStateHandler.class getMessageByCurrentStatePlusTwo method PrevState of telegramPerson : " +
                context.getTelegramPerson().getPrevStateName());

        MessageEntity messageEntity = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getTelegramPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        int middleStateInt = BotState.valueOf(context.getTelegramPerson().getCurrentStateName()).ordinal() + 2;
        context.getTelegramPerson().setPrevStateName(BotState.byId(middleStateInt - 1).name());
        context.getTelegramPerson().setCurrentStateName(BotState.byId(middleStateInt).name());
        return messageEntity;
    }

//    Получение тела сообщения пользователю по его предыдущему состоянию с присвоенеим в последующем пользователю
//    текущего сосотояния на 1 позиии назад и в соответствии текущему присвоить значение предыдущего состояния
    private MessageEntity getMessageByCurrentStateMinusOne (MessageEntityList messageEntityList, BotContext context){

        consoleLogger.info("START getMessageByCurrentStateMinusOne method of TelegramBotStateHandler.class");
        telegramLogger.info("TelegramBotStateHandler.class getMessageByCurrentStateMinusOne method PrevState of telegramPerson : " +
                context.getTelegramPerson().getPrevStateName());

        MessageEntity messageEntity =  messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getTelegramPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        context.getTelegramPerson().setPrevStateName(context.getTelegramPerson().getCurrentStateName());
        return messageEntity;
    }
}
