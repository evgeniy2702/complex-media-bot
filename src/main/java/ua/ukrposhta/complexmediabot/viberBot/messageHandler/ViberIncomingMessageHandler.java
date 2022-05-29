package ua.ukrposhta.complexmediabot.viberBot.messageHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.BotViber;
import ua.ukrposhta.complexmediabot.google.GoogleSheetsLive;
import ua.ukrposhta.complexmediabot.model.OutputMessage;
import ua.ukrposhta.complexmediabot.model.user.PersonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntityList;
import ua.ukrposhta.complexmediabot.service.SaxHandlerService;
import ua.ukrposhta.complexmediabot.service.SaxParserService;
import ua.ukrposhta.complexmediabot.utils.exception.SenderException;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForMessage;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ViberLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberSender;
import ua.ukrposhta.complexmediabot.viberBot.message.ViberInMessage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Handler. It handle incoming message from viber server, create current viberBot context, put and remove user
 * from temporary cache in view HashMap<long chat_id, ViberPersonEntity user>, identify language of user and to turn on appropriate
 * paths of files with message, buttons and errors
 */

@Component
@PropertySource(value = "classpath:properties/link.properties")
public class ViberIncomingMessageHandler {
    @Value("${path.xml.ukraine.message}")
    private String pathXmlUkraineMessage;
    @Value("${path.xml.english.message}")
    private String pathXmlEnglishMessage;
    @Value("${path.xml.unknown.message}")
    private String pathXmlUnknownMessage;

    @Value(value = "${path.xml.ukraine.button}")
    private String pathXmlUkraineButton;
    @Value("${path.xml.english.button}")
    private String pathXmlEnglishButton;
    @Value("${path.xml.unknown.button}")
    private String pathXmlUnknownButton;

    @Value("${path.xml.ukraine.error}")
    private String pathXmlUkraineError;
    @Value("${path.xml.english.error}")
    private String pathXmlEnglishError;

    @Value("${viber.piar.unit.first.receiver}")
    private String firstReceiverPiarUnit;

    @Value("${viber.piar.unit.second.receiver}")
    private String secondReceiverPiarUnit;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private GoogleSheetsLive googleSheetsLive;
    private Integer numberOfCellExcelSheet = 0;
    private SaxParserService parser;
    private SaxHandlerService handler;
    private BotLogger console;
    private BotLogger viberLogger;
    private BotViber viberBot;
    private ViberBotStateHandler viberBotStateHandler;
    private HashSet<String> language_codeList;
    private final ObjectMapper objectMapper;

    public ViberIncomingMessageHandler(GoogleSheetsLive googleSheetsLive,
                                       SaxParserService parser,
                                       SaxHandlerService handler,
                                       BotViber botViber,
                                       ViberBotStateHandler viberBotStateHandler,
                                       ObjectMapper objectMapper) {
        this.googleSheetsLive = googleSheetsLive;
        this.parser = parser;
        this.handler = handler;
        this.viberBot = botViber;
        this.viberBotStateHandler = viberBotStateHandler;
        this.console = BotLogger.getLogger(LoggerType.CONSOLE);
        this.viberLogger = ViberLogger.getLogger(LoggerType.VIBER);
        this.language_codeList = new HashSet<String>() {{
            add("unknown");
        }};
        this.objectMapper = objectMapper;
    }

    public void processingIncomingMessage(JsonNode json, Map<String, PersonEntity> personMap) throws JsonProcessingException {
        console.info("START processingIncomingMessage method in ViberIncomingMessageHandler.class");

        BotState state;

        if("conversation_started".equals(json.get("event").textValue())) {
            JsonNode jsonUser = json.get("user");
            ViberSender sender = objectMapper.treeToValue(jsonUser, ViberSender.class);
            ViberPersonEntity viberPerson = new ViberPersonEntity(sender);

            console.info("ViberIncomingMessageHandler.class  processingIncomingMessage json.get(\"event\") = conversation_started event: " + json.get("event").textValue() + " ;");
            viberLogger.info("ViberIncomingMessageHandler.class  processingIncomingMessage json.get(\"event\") = conversation_started event: " + json.get("event").textValue() + " ;");

            console.info(sender.toString());
            viberLogger.info(sender.toString());

            switchLanguage(viberPerson.getViberSender().getLanguage(), viberPerson, language_codeList);

            BotContext context = BotContext.ofViber(BotType.VIBER.getBotType(BotType.VIBER.getText()), viberBot, viberPerson, json.get("event").textValue());

            viberLogger.info("ViberIncomingMessageHandler.class  event = " + json.get("event") + " ; CurrentState : " + viberPerson.getCurrentStateName() + "; PrevState : " +
                    viberPerson.getPrevStateName());
            viberLogger.info("ViberIncomingMessageHandler.class event  = " + json.get("event").textValue());
            viberLogger.info("ViberIncomingMessageHandler.class  event = " + json.get("event").textValue() + "; viberPerson : " + context.getViberPerson().toString());
            viberLogger.info("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + "; context : " + context.toString());
            viberLogger.info("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + "; language_codeList : " + language_codeList.size());

            state = viberBotStateHandler.handleIncomingMessage(json, context, viberBot);

            viberLogger.info("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + "; STATE update.hasMessage() : " + state);

            personMap.put(viberPerson.getViberSender().getId(), viberPerson);

        }
        if("unsubscribed".equals(json.get("event").textValue())) {

            console.info("ViberIncomingMessageHandler.class  processingIncomingMessage json.get(\"event\") = unsubscribed event: " + json.get("event") + " ;");
            viberLogger.info("ViberIncomingMessageHandler.class  processingIncomingMessage json.get(\"event\") = unsubscribed event: " + json.get("event") + " ;");

            String userId = json.get("user_id").textValue();
            personMap.remove(userId);
        }
        if(json.get("message") != null){

            console.info("ViberIncomingMessageHandler.class  processingIncomingMessage json.get(\"message\") event: " + json.get("event").textValue() + " ;");
            viberLogger.info("ViberIncomingMessageHandler.class  processingIncomingMessage json.get(\"message\") event: " + json.get("event").textValue() + " ;");

            viberLogger.info("ViberIncomingMessageHandler.class  processingIncomingMessage message : " + json.get("message") + " ;");

            ViberInMessage inMessage = objectMapper.treeToValue(json, ViberInMessage.class);

            ViberSender sender = inMessage.getSender();

            ViberPersonEntity viberPerson = (ViberPersonEntity) personMap.get(sender.getId());

            BotContext context;

            if(viberPerson == null) {
                viberPerson = new ViberPersonEntity(sender);
                viberLogger.info("IF ViberIncomingMessageHandler.class processingIncomingMessage : " + viberPerson.toString());
                switchLanguage(viberPerson.getViberSender().getLanguage(), viberPerson, language_codeList);

            } else if (viberPerson.getViberSender().getLanguage().equals("unknown")){
                viberLogger.info("ELSE  ViberIncomingMessageHandler.class processingIncomingMessage : " + viberPerson.toString());
                switchLanguage(inMessage.getMessage().getText(), viberPerson, language_codeList);
            }

            viberLogger.info("ViberIncomingMessageHandler.class  processingIncomingMessage message : " + json.get("message") + " ; viberPerson : " + viberPerson);

             context = BotContext.ofViber(BotType.VIBER.getBotType(BotType.VIBER.getText()),viberBot, viberPerson, json.get("message").get("text").textValue());

            if (language_codeList
                    .contains(viberPerson.getViberSender().getLanguage())) {
                ifEndWorkWithBot(inMessage, viberPerson);
            }

            try {

                if (!viberPerson.getViberSender().getId().equals(firstReceiverPiarUnit) ||
                        !viberPerson.getViberSender().getId().equals(secondReceiverPiarUnit)) {

                    state = viberBotStateHandler.handleIncomingMessage(json,context,viberBot);

                    console.info("STATE event = " + json.get("event").textValue() + "; state " + state);

                    personMap.put(viberPerson.getViberSender().getId(), viberPerson);
                }

                viberLogger.info("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + "; user : " + viberPerson.toString());
                viberLogger.info("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + "; SUBJECT : " + viberPerson.getSubject() +
                        "; CURRENT_STATE_NAME : " + viberPerson.getCurrentStateName() + "; viberPerson.getSubject().equalsIgnoreCase(\"\") = " + viberPerson.getSubject().equalsIgnoreCase("") +
                        ";  viberPerson.getCurrentStateName().equals(BotState.END.name()) = " + viberPerson.getCurrentStateName().equals(BotState.END.name()));

                if (!viberPerson.getSubject().equalsIgnoreCase("") &&
                        (viberPerson.getCurrentStateName().equals(BotState.END.name()))) {
                    numberOfCellExcelSheet = googleSheetsLive.readNumberOfCellExcelSheetFromExcelSheet(context, numberOfCellExcelSheet);
                    if (!this.activeProfile.equalsIgnoreCase("dev")) {
                        sendNewRequestForPiar(context, firstReceiverPiarUnit);
                        sendNewRequestForPiar(context, secondReceiverPiarUnit);
                    }

                    viberLogger.info("SEND ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + " info about request firstChatIdPiarUnit : " + firstReceiverPiarUnit + " | secondChatIdPiarUnit : " +
                            secondReceiverPiarUnit);
                    personMap.remove(viberPerson.getViberSender().getId());
                    viberLogger.info("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + " size of cache language_codeList : " +
                            language_codeList.size());
                }

                viberLogger.info("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + ". Name of cell in excel google sheet : " + numberOfCellExcelSheet);

            } catch (Exception e) {
                console.error("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + " ERROR : " + e.getMessage());
                console.error("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + " CAUSE : " + e.getCause());
                viberLogger.error("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + " ERROR : " + e.getMessage());
                viberLogger.error("ViberIncomingMessageHandler.class event = " + json.get("event").textValue() + " CAUSE : " + e.getCause());
                throw new SenderException(context.getTypeBot(), e.getCause());
            }

        }
    }

    private void switchLanguage(String language, ViberPersonEntity viberPerson, HashSet<String> language_codeList) {
        console.info("START switchLanguage method in ViberIncomingMessageHandler.class");
        switch (language){
            case "uk-UA":
                viberPerson.getViberSender().setLanguage(language);
                viberPerson.setMessagePath(pathXmlUkraineMessage);
                viberPerson.setButtonPath(pathXmlUkraineButton);
                viberPerson.setErrorPath(pathXmlUkraineError);
                viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                viberPerson.setPrevStateName(BotState.START.name());
                viberPerson.setAddDate(LocalDateTime.now());
                break;
            case "Українська":
                viberPerson.getViberSender().setLanguage("uk-UA");
                viberPerson.setMessagePath(pathXmlUkraineMessage);
                viberPerson.setButtonPath(pathXmlUkraineButton);
                viberPerson.setErrorPath(pathXmlUkraineError);
                break;
            case "en-EN":
                viberPerson.getViberSender().setLanguage(language);
                viberPerson.setMessagePath(pathXmlEnglishMessage);
                viberPerson.setButtonPath(pathXmlEnglishButton);
                viberPerson.setErrorPath(pathXmlEnglishError);
                viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                viberPerson.setPrevStateName(BotState.START.name());
                viberPerson.setAddDate(LocalDateTime.now());
                break;
            case "English":
                viberPerson.getViberSender().setLanguage("en-EN");
                viberPerson.setMessagePath(pathXmlEnglishMessage);
                viberPerson.setButtonPath(pathXmlEnglishButton);
                viberPerson.setErrorPath(pathXmlEnglishError);
                break;
            default:
                viberPerson.getViberSender().setLanguage("unknown");
                viberPerson.setMessagePath(pathXmlUnknownMessage);
                viberPerson.setButtonPath(pathXmlUnknownButton);
                viberPerson.setErrorPath(pathXmlEnglishError);
                viberPerson.setCurrentStateName(BotState.LANGUAGE.name());
                viberPerson.setPrevStateName(BotState.LANGUAGE.name());
                viberPerson.setAddDate(LocalDateTime.now());
                break;
        }
        language_codeList.add(viberPerson.getViberSender().getLanguage());
        viberLogger.info("ViberIncomingMessageHandler.class LanguageCode of viberPerson : " +
                viberPerson.getViberSender().getLanguage());
        viberLogger.info("ViberIncomingMessageHandler.class size of language_codeList : " +
                language_codeList.size());
    }

    @SuppressWarnings("unchecked")
    private void ifEndWorkWithBot(ViberInMessage inMessage, ViberPersonEntity viberPerson) {

        console.info("START ifEndWorkWithBot method in ViberIncomingMessageHandler.class");


            String text = inMessage.getMessage().getText();

            MySaxHandlerForButtons buttonHandlerXml = (MySaxHandlerForButtons) handler
                    .getHandler(ParserHandlerType.BUTTON);

            ButtonEntityList buttonEntityList =
                    (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
                            .getObjectExchangeFromXML(viberPerson.getButtonPath(), buttonHandlerXml);

            buttonHandlerXml.setBotType(BotType.VIBER.name());


            List<ButtonEntity> listOfEndBtn = buttonEntityList.getButtonEntities().stream()
                    .map(btn -> {
                        if (btn.getType().equals(ButtonType.END.name()) ||
                                btn.getType().equals(ButtonType.END_WORK.name()))
                            return btn;
                        return null;
                    })
                    .filter(Objects::nonNull).collect(Collectors.toList());

            if (listOfEndBtn.stream().anyMatch(btn -> btn.getTxt().equals(text))) {
                BotState state = BotState.END;
                String statePrev = BotState.byId(state.ordinal() - 1).name();
                viberPerson.setExit(true);
                viberPerson.setCurrentStateName(state.name());
                viberPerson.setPrevStateName(statePrev);

            }


    }

    @SuppressWarnings("unchecked")
    private void sendNewRequestForPiar(BotContext context, String receiverPiarUnit) {

        console.info("START sendNewRequestForPiar method in ViberIncomingMessageHandler.class");

        MySaxHandlerForMessage messageHandlerXml = (MySaxHandlerForMessage) handler
                .getHandler(ParserHandlerType.MESSAGE);

        messageHandlerXml.setBotType(BotType.VIBER.name());

        MessageEntityList messageEntityList = (MessageEntityList) parser.getParser(ParserHandlerType.MESSAGE)
                .getObjectExchangeFromXML(context.getViberPerson().getMessagePath(), messageHandlerXml);

        MessageEntity PIAR_UNIT = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType().equals("PIAR_UNIT"))
                .collect(Collectors.toList()).get(0);

        if(!context.getViberPerson().getSubject().isEmpty()) {

            String sendText = PIAR_UNIT.getTxt() + "\n1) Назва : " +
                    context.getViberPerson().getMediaName() + ".\n2) Запит : " + context.getViberPerson().getSubject() +
                    "\n3) Посилання на таблицю запитів : " +
                    "https://docs.google.com/spreadsheets/d/1juzFkS2cZctAT7exY4N9fS4WfwrWZ3AVqweKJXAy0hE/edit#gid=0";

            OutputMessage outputMessage = OutputMessage.builder()
                    .chat_id(context.getViberPerson().getViberSender().getId())
                    .context(context)
                    .message_text(sendText)
                    .build();

            viberLogger.info("ViberIncomingMessageHandler.class : userId = " + outputMessage.toString() +
                    ";  message = " + sendText);

            try {

                context.getViberBot().send(outputMessage);

            } catch (SenderException e) {
                viberLogger.error("ViberIncomingMessageHandler.class ERROR : " + Arrays.asList(e.getStackTrace()));
            }
            viberLogger.info("ViberIncomingMessageHandler.class send message about request info to viber of piar unit ukrposhta with chatId : " + receiverPiarUnit);
        }
        viberLogger.info("ViberIncomingMessageHandler.class do not send message about request info to viber of piar unit ukrposhta because subject of requestMedia is null.");
    }
}

