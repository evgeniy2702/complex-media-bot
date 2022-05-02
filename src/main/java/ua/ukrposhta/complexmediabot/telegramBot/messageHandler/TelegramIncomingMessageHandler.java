package ua.ukrposhta.complexmediabot.telegramBot.messageHandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.google.GoogleSheetsLive;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomingMessage;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.Person;
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
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class is Handler. It handle incoming message from telegram server, create current bot context, put and remove user
 * from temporary cache in view HashMap<long chat_id, Person user>, identify language of user and to turn on appropriate
 * paths of files with message, buttons and errors
 */

@Component
@PropertySource(value = "classpath:properties/link.properties")
public class TelegramIncomingMessageHandler {

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

    @Value("${telegram.piar.unit.first.chatId}")
    private String firstChatIdPiarUnit;

    @Value("${telegram.piar.unit.second.chatId}")
    private String secondChatIdPiarUnit;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private GoogleSheetsLive googleSheetsLive;
    private Integer numberOfCellExcelSheet = 0;
    private SaxParserService parser;
    private SaxHandlerService handler;
    private BotLogger logger;
    private BotLogger telegramLogger;
    private TelegramBot bot;
    private BotStateHandler stateHandler;
    private HashSet<String> language_codeList;

    public TelegramIncomingMessageHandler(GoogleSheetsLive googleSheetsLive,
                                          SaxParserService parser,
                                          SaxHandlerService handler,
                                          TelegramBot bot,
                                          BotStateHandler stateHandler) {
        this.googleSheetsLive = googleSheetsLive;
        this.parser = parser;
        this.handler = handler;
        this.bot = bot;
        this.stateHandler = stateHandler;
        this.logger = BotLogger.getLogger(LoggerType.CONSOLE);
        this.telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);
        this.language_codeList = new HashSet<String>(){{
            add("unknown");
        }};
    }

    public void processingIncomingMessage(Update update, Map<Long, Person> personMap){
            logger.info("START processingIncomingMessage method in TelegramIncomingMessageHandler.class");
            if(update.hasMessage()) {
                if(update.getMessage().hasText()){
                    final String text = update.getMessage().getText();
                    final Long chat_id = update.getMessage().getChatId();
                    final String language = update.getMessage().getFrom().getLanguageCode();

                    IncomingMessage incomingTelegramMessage = IncomingMessage.builder()
                            .chat_id(chat_id)
                            .firstName(update.getMessage().getFrom().getFirstName())
                            .isBot(update.getMessage().getFrom().getIsBot())
                            .lastName(update.getMessage().getFrom().getLastName())
                            .languageCode(language)
                            .userName(update.getMessage().getFrom().getUserName())
                            .text(text)
                            .build();

                    logger.info(incomingTelegramMessage.toString());
                    telegramLogger.info(incomingTelegramMessage.toString());

                    Person person = personMap.get(chat_id);

                    BotContext context;
                    BotState state;

                    if(person == null){
                        person = new Person(incomingTelegramMessage);
                        telegramLogger.info("IF TelegramIncomingMessageHandler.class processingIncomingMessage : " + person.toString());
                        switchLanguage(language, person, language_codeList);
                        context = BotContext.of(BotType.TELEGRAM.getBotType(BotType.TELEGRAM.getText()), bot, person, text);

                    } else {
                        telegramLogger.info("ELSE  TelegramIncomingMessageHandler.class processingIncomingMessage : " + person.toString());
                        if(person.getIncomingTelegramMessage().getLanguageCode().equals("unknown"))
                            switchLanguage(text, person, language_codeList);
                        context = BotContext.of(BotType.TELEGRAM.getBotType(BotType.TELEGRAM.getText()), bot, person, text);
                    }


                    telegramLogger.info("TelegramIncomingMessageHandler.class CurrentState : " + context.getPerson().getCurrentStateName() + "; PrevState : " +
                                    context.getPerson().getPrevStateName());
                    telegramLogger.info("TelegramIncomingMessageHandler.class text : " + text);
                    telegramLogger.info("TelegramIncomingMessageHandler.class person : " + context.getPerson().toString());
                    telegramLogger.info("TelegramIncomingMessageHandler.class context : " + context.toString());
                    telegramLogger.info("TelegramIncomingMessageHandler.class language_codeList : " + language_codeList.size());

                    if(language_codeList
                            .contains(person.getIncomingTelegramMessage().getLanguageCode())) {
                        ifEndWorkWithBot(update, person);
                    }

                    try {

                        if(person.getIncomingTelegramMessage().getChat_id() != Long.parseLong(firstChatIdPiarUnit) ||
                                person.getIncomingTelegramMessage().getChat_id() != Long.parseLong(secondChatIdPiarUnit)) {

                            state = stateHandler.handleIncomingMessage(update, context, bot);

                            logger.info("STATE update.hasMessage() : " + state);
                            personMap.put(chat_id, person);

                        }

                        telegramLogger.info("TelegramIncomingMessageHandler.class user : " + person.toString());

                        if(!person.getSubject().equalsIgnoreCase("") &&
                                (person.getCurrentStateName().equals(BotState.END.name()))) {
                            numberOfCellExcelSheet = googleSheetsLive.readNumberOfCellExcelSheetFromExcelSheet(context, numberOfCellExcelSheet);
                            if(!this.activeProfile.equalsIgnoreCase("dev")) {
                                sendNewRequestForPiar(context, firstChatIdPiarUnit);
                                sendNewRequestForPiar(context, secondChatIdPiarUnit);
                            }

                            telegramLogger.info("SEND TelegramIncomingMessageHandler.class info about request firstChatIdPiarUnit : " + firstChatIdPiarUnit + " | secondChatIdPiarUnit : " +
                                    secondChatIdPiarUnit);
                            personMap.remove(chat_id);
                            telegramLogger.info("TelegramIncomingMessageHandler.class size of cache HashMap<Long, Person> : " +
                                    language_codeList.size());
                        }

                        telegramLogger.info("TelegramIncomingMessageHandler.class Name of cell in excel google sheet : " + numberOfCellExcelSheet);

                    } catch (Exception e) {
                        logger.error("TelegramIncomingMessageHandler.class ERROR : " + e.getMessage());
                        logger.error("TelegramIncomingMessageHandler.class CAUSE : " + e.getCause());
                        telegramLogger.error("TelegramIncomingMessageHandler.class ERROR : " + e.getMessage());
                        telegramLogger.error("TelegramIncomingMessageHandler.class CAUSE : " + e.getCause());
                         throw new SenderException(context.getTypeBot(), e.getCause());
                    }
                }
            }
            if(update.hasCallbackQuery()){
                Person person = personMap.get(update.getCallbackQuery().getFrom().getId());
                if((person.getIncomingTelegramMessage().getLastName() + " " +
                        person.getIncomingTelegramMessage().getFirstName())
                        .equals(update.getCallbackQuery().getData())){
                    BotContext context = BotContext.of(BotType.TELEGRAM.getBotType(BotType.TELEGRAM.getText()),
                            bot, person, update.getCallbackQuery().getData());
                    BotState state = stateHandler.handleIncomingMessage(update, context, bot);
                    telegramLogger.info("TelegramIncomingMessageHandler.class state update.hasCallbackQuery() : " + state);
                }
            }
        }

        private void switchLanguage(String language, Person person, HashSet<String> language_codeList) {
            logger.info("START switchLanguage method in TelegramIncomingMessageHandler.class");
            switch (language){
                case "uk":
                    person.getIncomingTelegramMessage().setLanguageCode(language);
                    person.setMessagePath(pathXmlUkraineMessage);
                    person.setButtonPath(pathXmlUkraineButton);
                    person.setErrorPath(pathXmlUkraineError);
                    person.setCurrentStateName(BotState.GOOD_DAY.name());
                    person.setPrevStateName(BotState.START.name());
                    person.setAddDate(LocalDateTime.now());
                    break;
                case "Українська":
                    person.getIncomingTelegramMessage().setLanguageCode("uk");
                    person.setMessagePath(pathXmlUkraineMessage);
                    person.setButtonPath(pathXmlUkraineButton);
                    person.setErrorPath(pathXmlUkraineError);
                    break;
                case "en":
                    person.getIncomingTelegramMessage().setLanguageCode(language);
                    person.setMessagePath(pathXmlEnglishMessage);
                    person.setButtonPath(pathXmlEnglishButton);
                    person.setErrorPath(pathXmlEnglishError);
                    person.setCurrentStateName(BotState.GOOD_DAY.name());
                    person.setPrevStateName(BotState.START.name());
                    person.setAddDate(LocalDateTime.now());
                    break;
                case "English":
                    person.getIncomingTelegramMessage().setLanguageCode("en");
                    person.setMessagePath(pathXmlEnglishMessage);
                    person.setButtonPath(pathXmlEnglishButton);
                    person.setErrorPath(pathXmlEnglishError);
                    break;
                default:
                    person.getIncomingTelegramMessage().setLanguageCode("unknown");
                    person.setMessagePath(pathXmlUnknownMessage);
                    person.setButtonPath(pathXmlUnknownButton);
                    person.setErrorPath(pathXmlEnglishError);
                    person.setCurrentStateName(BotState.LANGUAGE.name());
                    person.setPrevStateName(BotState.LANGUAGE.name());
                    person.setAddDate(LocalDateTime.now());
                    break;
            }
            language_codeList.add(person.getIncomingTelegramMessage().getLanguageCode());
            telegramLogger.info("TelegramIncomingMessageHandler.class LanguageCode of person : " +
                    person.getIncomingTelegramMessage().getLanguageCode());
            telegramLogger.info("TelegramIncomingMessageHandler.class size of language_codeList : " +
                    language_codeList.size());
        }

        @SuppressWarnings("unchecked")
        private void ifEndWorkWithBot(Update update, Person person) {

            logger.info("START ifEndWorkWithBot method in TelegramBot.class");

            if (update.getCallbackQuery() == null) {
                String text = update.getMessage().getText();

                MySaxHandlerForButtons buttonHandlerXml = (MySaxHandlerForButtons) handler
                        .getHandler(ParserHandlerType.BUTTON);

                buttonHandlerXml.setBotType(BotType.TELEGRAM.name());

                ButtonEntityList buttonEntityList =
                        (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
                        .getObjectExchangeFromXML(person.getButtonPath(), buttonHandlerXml);


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
                    person.setCurrentStateName(state.name());
                    person.setPrevStateName(statePrev);
                    person.setExit(true);
                }
            }

        }

    @SuppressWarnings("unchecked")
    private void sendNewRequestForPiar(BotContext context, String chatIdPiarUnit) {

        logger.info("START sendNewRequestForPiar method in TelegramBot.class");

        MySaxHandlerForMessage messageHandlerXml = (MySaxHandlerForMessage) handler
                .getHandler(ParserHandlerType.MESSAGE);

        messageHandlerXml.setBotType(BotType.TELEGRAM.name());

        MessageEntityList messageEntityList = (MessageEntityList) parser.getParser(ParserHandlerType.MESSAGE)
                .getObjectExchangeFromXML(context.getPerson().getMessagePath(), messageHandlerXml);

        MessageEntity PIAR_UNIT = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType().equals("PIAR_UNIT"))
                .collect(Collectors.toList()).get(0);

        if(!context.getPerson().getSubject().isEmpty()) {

            String sendText = PIAR_UNIT.getTxt() + "\n1) Назва : " +
                    context.getPerson().getMediaName() + ".\n2) Запит : " + context.getPerson().getSubject() +
                    "\n3) Посилання на таблицю запитів : " +
                    "https://docs.google.com/spreadsheets/d/1juzFkS2cZctAT7exY4N9fS4WfwrWZ3AVqweKJXAy0hE/edit#gid=0";

            SendMessage message = new SendMessage(chatIdPiarUnit, sendText);

            telegramLogger.info("TelegramIncomingMessageHandler.class : chatId = " + message.toString() +
                    ";  message = " + message.getText());

            try {

                context.getBot().execute(message);

            } catch (TelegramApiException e) {
                telegramLogger.error("TelegramIncomingMessageHandler.class ERROR : " + Arrays.asList(e.getStackTrace()));
            }
            telegramLogger.info("TelegramIncomingMessageHandler.class send message about request info to telegram of piar unit ukrposhta with chatId : " + chatIdPiarUnit);
        }
        telegramLogger.info("TelegramIncomingMessageHandler.class do not send message about request info to telegram of piar unit ukrposhta because subject of requestMedia is null.");
    }

}
