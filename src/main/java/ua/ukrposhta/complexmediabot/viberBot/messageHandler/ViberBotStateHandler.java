package ua.ukrposhta.complexmediabot.viberBot.messageHandler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.BotViber;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.AbstractEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot.ErrorEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntityList;
import ua.ukrposhta.complexmediabot.service.SaxHandlerService;
import ua.ukrposhta.complexmediabot.service.SaxParserService;
import ua.ukrposhta.complexmediabot.model.OutputMessage;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForErrors;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForMessage;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ViberLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;
import ua.ukrposhta.complexmediabot.viberBot.servic.ViberKeyboardService;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Service. It create keyboard and fill it by buttons according state of user .
 */


@Component
public class ViberBotStateHandler {

    private SaxHandlerService handler;
    private SaxParserService parser;
    private ViberKeyboardService keyboard;

    private BotLogger viberLogger = ViberLogger.getLogger(LoggerType.VIBER);
    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);

    public ViberBotStateHandler(SaxHandlerService handler,
                           SaxParserService parser,
                                ViberKeyboardService keyboard) {
        this.handler = handler;
        this.parser = parser;
        this.keyboard = keyboard;
    }

    //    Обработчик состояния пользователя
    @SuppressWarnings("unchecked")
    public BotState handleIncomingMessage(JsonNode json, BotContext context, BotViber bot){

        MySaxHandlerForButtons handlerForButtons = (MySaxHandlerForButtons) handler.getHandler(ParserHandlerType.BUTTON);
        MySaxHandlerForMessage handlerForMessage = (MySaxHandlerForMessage) handler.getHandler(ParserHandlerType.MESSAGE);
        MySaxHandlerForErrors handlerForErrors = (MySaxHandlerForErrors) handler.getHandler(ParserHandlerType.ERROR);

        handlerForButtons.setBotType(BotType.VIBER.name());
        handlerForErrors.setBotType(BotType.VIBER.name());
        handlerForMessage.setBotType(BotType.VIBER.name());


        MessageEntityList messageEntityList = (MessageEntityList) parser.getParser(ParserHandlerType.MESSAGE)
                .getObjectExchangeFromXML(context.getViberPerson().getMessagePath(),handlerForMessage);

        ButtonEntityList buttonEntityList = (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
                .getObjectExchangeFromXML(context.getViberPerson().getButtonPath(), handlerForButtons);

        ErrorEntityList errorEntityList = (ErrorEntityList) parser.getParser(ParserHandlerType.ERROR)
                .getObjectExchangeFromXML(context.getViberPerson().getErrorPath(), handlerForErrors);

        String event = json.get("event").textValue();
        Pattern pattern;
        Matcher matcher;
        MessageEntity messageEntity;
        ErrorEntity errorEntity;
        OutputMessage outputMessage;
        BotState state = BotState.valueOf(context.getViberPerson().getCurrentStateName());

        consoleLogger.info("START handleIncomingMessage method in ViberBotStateHandler.class event: " + event + " ; ");
        viberLogger.info("START handleIncomingMessage method in ViberBotStateHandler.class event: " + event + " ; ");

        switch (state){
            case GOOD_DAY:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.GOOD_DAY.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                messageEntity = getMessageByCurrentStateMinusOne(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                messageEntity = getMessageByCurrentStatePlusTwo(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case LANGUAGE:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.LANGUAGE.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                messageEntity = getMessageByCurrentStatePlusTwo(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case SELECT:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.SELECT.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                handlerOfError(messageEntityList, errorEntityList, buttonEntityList, keyboard, context, bot);

                break;
            case MEDIA:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.MEDIA.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                handlerOfError(messageEntityList, errorEntityList, buttonEntityList, keyboard, context, bot);

                break;
            case NAME_SURNAME:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.NAME_SURNAME.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                context.getViberPerson().setMediaName(json.get("message").get("text").textValue());

                messageEntity = getMessageByCurrentState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case PHONE:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.PHONE.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                context.getViberPerson().setName_surname(json.get("message").get("text").textValue());

                messageEntity = getMessageByCurrentState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                break;
            case EMAIL:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.EMAIL.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                pattern = Pattern.compile("^\\+380[0-9]{2} [0-9]{3} [0-9]{2} [0-9]{2}$");
                matcher = pattern.matcher(json.get("message").get("text").textValue());
                if(matcher.find()){
                    String phone = json.get("message").get("text").textValue();
                    String phoneFormater = " " + phone.split(" ")[0].substring(0,3) + "(" +
                            phone.split(" ")[0].substring(3,6) +") " +
                            phone.split(" ")[1] + " " +
                            phone.split(" ")[2] + " " +
                            phone.split(" ")[3];
                    context.getViberPerson().setPhone(phoneFormater);

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
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.SUBJECT.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                pattern = Pattern.compile("^([A-Za-z0-9_-]+\\.)*[A-Za-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,3}$");
                matcher = pattern.matcher(json.get("message").get("text").textValue());
                if(matcher.find()){
                    String email = json.get("message").get("text").textValue();
                    context.getViberPerson().setEmail(email);

                    messageEntity = getMessageByCurrentState(messageEntityList, context);
                    outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                    context.getViberPerson().setEmail(email);

                    bot.send(outputMessage);

                } else {
                    errorEntity = getErrorMessageByStateName(errorEntityList, context);
                    outputMessage = makeOutputMessage(errorEntity, keyboard, context);

                    bot.send(outputMessage);
                }
                break;

            case WE_CONTACT:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.WE_CONTACT.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                context.getViberPerson().setSubject(json.get("message").get("text").textValue());
                messageEntity = getMessageByCurrentState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                if(context.getViberPerson().isExit())
                    context.getViberPerson().setExit(false);

                bot.send(outputMessage);
                break;
            case END:
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + BotState.END.name());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; CurrentState is : " + context.getViberPerson().getCurrentStateName());
                viberLogger.info("ViberBotStateHandler.class event: " + event + " ; PrevState is : " + context.getViberPerson().getPrevStateName());

                if (context.getViberPerson().isExit()) {
                    context.getViberPerson().setActivity(false);
                } else {
                    context.getViberPerson().setActivity(true);
                }
                messageEntity = getMessageForEndState(messageEntityList, context);
                outputMessage = makeOutputMessage(messageEntity, keyboard, context);

                bot.send(outputMessage);

                switchLanguage(context.getViberPerson(), event);
                break;
        }

        return BotState.valueOf(context.getViberPerson().getCurrentStateName());

        }

//      Устанавливает текущее и предыдущее состояния пользователя в соответствии с имеющимся у пользователя
//      language_code
    private void switchLanguage( ViberPersonEntity viberPerson, String event) {
        consoleLogger.info("START switchLanguage method of ViberBotStateHandler.class event: " + event + " ; ");
        switch (viberPerson.getViberSender().getLanguage()){
            case "uk-UA":
                viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                viberPerson.setPrevStateName(BotState.START.name());
                viberPerson.setAddDate(LocalDateTime.now());
                break;
            case "en-EN":
                viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                viberPerson.setPrevStateName(BotState.START.name());
                viberPerson.setAddDate(LocalDateTime.now());
                break;
            default:
                viberPerson.getViberSender().setLanguage("unknown");
                viberPerson.setCurrentStateName(BotState.LANGUAGE.name());
                viberPerson.setPrevStateName(BotState.LANGUAGE.name());
                break;
        }
        viberLogger.info("ViberBotStateHandler.class event: " + event + " ; LanguageCode of viberPerson is : " +
                viberPerson.getViberSender().getLanguage());
    }

    //   Формирование исходящего JSON объекта для телеграмм бота в соответствии с состояниями пользователя
    private OutputMessage makeOutputMessage(AbstractEntity abstractEntity, ViberKeyboardService keyboardService, BotContext context) {
        consoleLogger.info("START makeOutputMessage method of ViberBotStateHandler.class");
        viberLogger.info("ViberBotStateHandler.class makeOutputMessage method CurrentState of viberPerson : " + context.getViberPerson().getCurrentStateName());
        return OutputMessage.builder()
                .chat_id(context.getViberPerson().getViberSender().getId())
                .context(context)
                .message_text(abstractEntity.getTxt())
                .viberKeyboard(keyboardService.getViberKeyboard(context))
                .build();

    }

//    Формирует сообщение пользователю согласно его текущему состоянию с переводом текущего состояния на +1 позицию
//    вперед и соответсвенно ему присвоение предыдущего состояния пользователю
    private MessageEntity getMessageByCurrentState ( MessageEntityList messageEntityList, BotContext context){
        consoleLogger.info("START getMessageByCurrentState method of ViberBotStateHandler.class");
        viberLogger.info("ViberBotStateHandler.class getMessageByCurrentState method CurrentState of viberPerson : " +
                context.getViberPerson().getCurrentStateName());
        MessageEntity messageEntity = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getViberPerson().getCurrentStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        context.getViberPerson().setPrevStateName(context.getViberPerson().getCurrentStateName());
        int middleStateInt = BotState.valueOf(context.getViberPerson().getCurrentStateName()).ordinal() + 1;
        context.getViberPerson().setCurrentStateName(BotState.byId(middleStateInt).name());
        return messageEntity;
    }

    //    Формирует сообщение пользователю согласно его текущему состоянию соответствующему BotState.END
    private MessageEntity getMessageForEndState ( MessageEntityList messageEntityList, BotContext context){
        consoleLogger.info("START getMessageForEndState method of ViberBotStateHandler.class");
        viberLogger.info("ViberBotStateHandler.class getMessageForEndState method CurrentState of viberPerson : " +
                context.getViberPerson().getCurrentStateName());
        return messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getViberPerson().getCurrentStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
    }

    //    Формирует сообщение об ошибке на основании предыдущего сосотояния пользователя с приведением текущее и
    //    предыдущее состояние к предыдущим значениям
    private ErrorEntity getErrorMessageByStateName(ErrorEntityList errorEntityList, BotContext context){
        consoleLogger.info("START getErrorMessageByStateName method of ViberBotStateHandler.class");
        viberLogger.info("ViberBotStateHandler.class getErrorMessageByStateName method PrevState of viberPerson : " +
                context.getViberPerson().getPrevStateName());
        return errorEntityList.getErrorEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getViberPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
    }

    //  Проверяет состояние пользователя на соответствие BotState.ERROR
    private boolean hasErrorInInput(BotContext context){
        for (BotState stateItem : BotState.values() ) {
            if( context.getInput().equals(stateItem.getName()) ||
                    context.getInput().equals(ButtonType.UA.getText()) ||
                    context.getInput().equals(ButtonType.EN.getText()) ) {
                consoleLogger.info("FALSE in hasErrorInInput method of ViberBotStateHandler.class");
                viberLogger.info("FALSE in hasErrorInInput method of ViberBotStateHandler.class");
                return false;
            }
        }
        consoleLogger.info("TRUE in hasErrorInInput method of ViberBotStateHandler.class");
        viberLogger.info("TRUE in hasErrorInInput method of ViberBotStateHandler.class");
        return true;
    }

    private boolean hasErrorInButton(BotContext context, ButtonEntityList buttonEntityList){
        for (ButtonEntity button : buttonEntityList.getButtonEntities() ) {
            if( context.getInput().equals(button.getTxt())){
                consoleLogger.info("FALSE in hasErrorInButton method of ViberBotStateHandler.class");
                viberLogger.info("FALSE in hasErrorInButton method of ViberBotStateHandler.class");
                return false;
            }
        }
        consoleLogger.info("TRUE in hasErrorInButton method of ViberBotStateHandler.class");
        viberLogger.info("TRUE in hasErrorInButton method of ViberBotStateHandler.class");
        return true;
    }

    //    Обработчик , который при присутсвии ошибок формирует и отправляет пользователю сообщение об ошибке
    //    либо формирует сообщение в соответствии с отправленным от него выбором
    private void handlerOfError(MessageEntityList messageEntityList, ErrorEntityList errorEntityList,
                                ButtonEntityList buttonEntityList, ViberKeyboardService keyboardService,
                                BotContext context, BotViber bot){
        consoleLogger.info("START handlerOfError method of ViberBotStateHandler.class");

        if(!hasErrorInInput(context) || !hasErrorInButton(context, buttonEntityList)){
            viberLogger.info("THERE IS NO errors in handlerOfError method of ViberBotStateHandler.class");
            MessageEntity messageEntity = getMessageByCurrentState(messageEntityList, context);
            OutputMessage outputMessage = makeOutputMessage(messageEntity,keyboardService, context);
            bot.send(outputMessage);
        } else {
            viberLogger.info("THERE IS errors in handlerOfError method of TelegramBotStateHandler.class");
            ErrorEntity errorEntity = getErrorMessageByStateNameEqualsERROR(errorEntityList);
            OutputMessage outputMessage = makeOutputMessage(errorEntity, keyboardService, context);
            bot.send(outputMessage);
        }

    }

    //    Выбор сообщения об ошибке (PHONE, EMAIL, ERROR) в соответствии с текущим состоянием пользователя
    private ErrorEntity getErrorMessageByStateNameEqualsERROR(ErrorEntityList errorEntityList){
        consoleLogger.info("START getErrorMessageByStateNameEqualsERROR method of ViberBotStateHandler.class");
        viberLogger.info("ERROR : " + BotState.ERROR.name() +
                " in getErrorMessageByStateNameEqualsERROR method of ViberBotStateHandler.class");
        return errorEntityList.getErrorEntities().stream()
                .filter(err -> err.getType()
                        .equals(BotState.ERROR.name()))
                .collect(Collectors.toList())
                .get(0);
    }


    //    Получение тела сообщения пользователю по его предыдущему состоянию с присвоенеим в последующем пользователю
    //    текущего сосотояния на 2 позиии вперед и в соответствии текущему присвоить значение предыдущего состояния
    private MessageEntity getMessageByCurrentStatePlusTwo ( MessageEntityList messageEntityList, BotContext context){

        consoleLogger.info("START getMessageByCurrentStatePlusTwo method of ViberBotStateHandler.class");
        viberLogger.info("ViberBotStateHandler.class getMessageByCurrentStatePlusTwo method PrevState of getViberPerson : " +
                context.getViberPerson().getPrevStateName());

        MessageEntity messageEntity = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getViberPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        int middleStateInt = BotState.valueOf(context.getViberPerson().getCurrentStateName()).ordinal() + 2;
        context.getViberPerson().setPrevStateName(BotState.byId(middleStateInt - 1).name());
        context.getViberPerson().setCurrentStateName(BotState.byId(middleStateInt).name());
        return messageEntity;
    }

    //    Получение тела сообщения пользователю по его предыдущему состоянию с присвоенеим в последующем пользователю
    //    текущего сосотояния на 1 позиии назад и в соответствии текущему присвоить значение предыдущего состояния
    private MessageEntity getMessageByCurrentStateMinusOne (MessageEntityList messageEntityList, BotContext context){

        consoleLogger.info("START getMessageByCurrentStateMinusOne method of TelegramBotStateHandler.class");
        viberLogger.info("TelegramBotStateHandler.class getMessageByCurrentStateMinusOne method PrevState of getViberPerson : " +
                context.getViberPerson().getPrevStateName());

        MessageEntity messageEntity =  messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType()
                        .equals(BotState.valueOf(context.getViberPerson().getPrevStateName()).name()))
                .collect(Collectors.toList())
                .get(0);
        context.getViberPerson().setPrevStateName(context.getViberPerson().getCurrentStateName());
        return messageEntity;
    }

}