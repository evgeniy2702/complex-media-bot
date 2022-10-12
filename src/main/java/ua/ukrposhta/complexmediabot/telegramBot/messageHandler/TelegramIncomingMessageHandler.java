package ua.ukrposhta.complexmediabot.telegramBot.messageHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ua.ukrposhta.complexmediabot.model.user.PersonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot.ButtonEntityList;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntity;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot.MessageEntityList;
import ua.ukrposhta.complexmediabot.service.SaxHandlerService;
import ua.ukrposhta.complexmediabot.service.SaxParserService;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomTelegramMessage;
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
 * This class is Handler. It handle incoming message from telegram server, create current telegramBot context, put and remove user
 * from temporary cache in view HashMap<long chat_id, TelegramPersonEntity user>, identify language of user and to turn on appropriate
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

//    @Value("${telegram.piar.unit.first.chatId}")
//    private String firstChatIdPiarUnit;
//
//    @Value("${telegram.piar.unit.second.chatId}")
//    private String secondChatIdPiarUnit;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private GoogleSheetsLive googleSheetsLive;
    private Integer numberOfCellExcelSheet = 0;
    private SaxParserService parser;
    private SaxHandlerService handler;
    private BotLogger logger;
    private BotLogger telegramLogger;
    private TelegramBot telegramBot;
    private TelegramBotStateHandler stateHandler;
    private HashSet<String> language_codeList;

    public TelegramIncomingMessageHandler(GoogleSheetsLive googleSheetsLive,
                                          SaxParserService parser,
                                          SaxHandlerService handler,
                                          TelegramBot botTelegram,
                                          TelegramBotStateHandler stateHandler) {
        this.googleSheetsLive = googleSheetsLive;
        this.parser = parser;
        this.handler = handler;
        this.telegramBot = botTelegram;
        this.stateHandler = stateHandler;
        this.logger = BotLogger.getLogger(LoggerType.CONSOLE);
        this.telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);
        this.language_codeList = new HashSet<String>(){{
            add("unknown");
        }};
    }

    public void processingIncomingMessage(Update update,
                                          Map<String, PersonEntity> personMap,
                                          Map<String, String> piars) throws JsonProcessingException {
        logger.info("START processingIncomingMessage method in TelegramIncomingMessageHandler.class");

        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                final String text = update.getMessage().getText();
                final String chat_id = String.valueOf(update.getMessage().getChatId());
                final String language = update.getMessage().getFrom().getLanguageCode();

                IncomTelegramMessage incomingTelegramMessage = IncomTelegramMessage.builder()
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

                TelegramPersonEntity telegramPerson = (TelegramPersonEntity) personMap.get(String.valueOf(chat_id));

                BotContext context;
                BotState state;

                if (telegramPerson == null) {
                    telegramPerson = new TelegramPersonEntity(incomingTelegramMessage);
                    telegramLogger.info("IF TelegramIncomingMessageHandler.class processingIncomingMessage : " + telegramPerson.toString());
                    switchLanguage(language, telegramPerson, language_codeList);
                } else {
                    telegramLogger.info("ELSE  TelegramIncomingMessageHandler.class processingIncomingMessage : " + telegramPerson.toString());
                    if (telegramPerson.getIncomTelegramMessage().getLanguageCode().equals("unknown"))
                        switchLanguage(text, telegramPerson, language_codeList);
                }

                context = BotContext.ofTelegram(BotType.TELEGRAM.getBotType(BotType.TELEGRAM.getText()), telegramBot, telegramPerson, text);

                telegramLogger.info("TelegramIncomingMessageHandler.class CurrentState : " + telegramPerson.getCurrentStateName() + "; PrevState : " +
                        telegramPerson.getPrevStateName());
                telegramLogger.info("TelegramIncomingMessageHandler.class text : " + text);
                telegramLogger.info("TelegramIncomingMessageHandler.class telegramPerson : " + context.getTelegramPerson().toString());
                telegramLogger.info("TelegramIncomingMessageHandler.class context : " + context.toString());
                telegramLogger.info("TelegramIncomingMessageHandler.class language_codeList : " + language_codeList.size());

                if (language_codeList
                        .contains(telegramPerson.getIncomTelegramMessage().getLanguageCode())) {
                    ifEndWorkWithBot(update, telegramPerson);
                }

                try {

                    if (!piars.containsKey(telegramPerson.getIncomTelegramMessage().getChat_id())) {

                        state = stateHandler.handleIncomingMessage(update, context, telegramBot);

                        logger.info("STATE update.hasMessage() : " + state);
                        personMap.put(String.valueOf(chat_id), telegramPerson);
                        telegramBot.getTelegramPersons().put(String.valueOf(chat_id), telegramPerson);
                    }

                    telegramLogger.info("TelegramIncomingMessageHandler.class user : " + telegramPerson.toString());

                    if (!telegramPerson.getSubject().equalsIgnoreCase("") &&
                            (telegramPerson.getCurrentStateName().equals(BotState.END.name()))) {
                        numberOfCellExcelSheet = googleSheetsLive.readNumberOfCellExcelSheetFromExcelSheet(context, numberOfCellExcelSheet);
                        if (!this.activeProfile.equalsIgnoreCase("dev")) {
                            for(String key : piars.keySet()) {
                                sendNewRequestForPiar(context, key);
                                telegramLogger.info("SEND TelegramIncomingMessageHandler.class info about request USER_ID : " + key);
                            }
                        }
                        personMap.remove(String.valueOf(chat_id));
                        telegramBot.getTelegramPersons().remove(String.valueOf(chat_id), telegramPerson);
                        telegramLogger.info("TelegramIncomingMessageHandler.class size of cache language_codeList : " +
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
        if (update.hasCallbackQuery()) {
            TelegramPersonEntity telegramPerson = (TelegramPersonEntity) personMap
                    .get(String.valueOf(update.getCallbackQuery().getFrom().getId()));
            if ((telegramPerson.getIncomTelegramMessage().getLastName() + " " +
                    telegramPerson.getIncomTelegramMessage().getFirstName())
                    .equals(update.getCallbackQuery().getData())) {
                BotContext context = BotContext.ofTelegram(BotType.TELEGRAM.getBotType(BotType.TELEGRAM.getText()),
                        telegramBot, telegramPerson, update.getCallbackQuery().getData());
                BotState state = stateHandler.handleIncomingMessage(update, context, telegramBot);
                telegramLogger.info("TelegramIncomingMessageHandler.class state update.hasCallbackQuery() : " + state);
            }
        }
    }


    private void switchLanguage(String language, TelegramPersonEntity telegramPerson, HashSet<String> language_codeList) {
            logger.info("START switchLanguage method in TelegramIncomingMessageHandler.class");
            switch (language){
                case "uk":
                    telegramPerson.getIncomTelegramMessage().setLanguageCode(language);
                    telegramPerson.setMessagePath(pathXmlUkraineMessage);
                    telegramPerson.setButtonPath(pathXmlUkraineButton);
                    telegramPerson.setErrorPath(pathXmlUkraineError);
                    telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                    telegramPerson.setPrevStateName(BotState.START.name());
                    telegramPerson.setAddDate(LocalDateTime.now());
                    break;
                case "Українська":
                    telegramPerson.getIncomTelegramMessage().setLanguageCode("uk");
                    telegramPerson.setMessagePath(pathXmlUkraineMessage);
                    telegramPerson.setButtonPath(pathXmlUkraineButton);
                    telegramPerson.setErrorPath(pathXmlUkraineError);
                    break;
                case "en":
                    telegramPerson.getIncomTelegramMessage().setLanguageCode(language);
                    telegramPerson.setMessagePath(pathXmlEnglishMessage);
                    telegramPerson.setButtonPath(pathXmlEnglishButton);
                    telegramPerson.setErrorPath(pathXmlEnglishError);
                    telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
                    telegramPerson.setPrevStateName(BotState.START.name());
                    telegramPerson.setAddDate(LocalDateTime.now());
                    break;
                case "English":
                    telegramPerson.getIncomTelegramMessage().setLanguageCode("en");
                    telegramPerson.setMessagePath(pathXmlEnglishMessage);
                    telegramPerson.setButtonPath(pathXmlEnglishButton);
                    telegramPerson.setErrorPath(pathXmlEnglishError);
                    break;
                default:
                    telegramPerson.getIncomTelegramMessage().setLanguageCode("unknown");
                    telegramPerson.setMessagePath(pathXmlUnknownMessage);
                    telegramPerson.setButtonPath(pathXmlUnknownButton);
                    telegramPerson.setErrorPath(pathXmlEnglishError);
                    telegramPerson.setCurrentStateName(BotState.LANGUAGE.name());
                    telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
                    telegramPerson.setAddDate(LocalDateTime.now());
                    break;
            }
            language_codeList.add(telegramPerson.getIncomTelegramMessage().getLanguageCode());
            telegramLogger.info("TelegramIncomingMessageHandler.class LanguageCode of telegramPerson : " +
                    telegramPerson.getIncomTelegramMessage().getLanguageCode());
            telegramLogger.info("TelegramIncomingMessageHandler.class size of language_codeList : " +
                    language_codeList.size());
        }

        @SuppressWarnings("unchecked")
    private void ifEndWorkWithBot(Update update, TelegramPersonEntity telegramPerson) {

            logger.info("START ifEndWorkWithBot method in TelegramBot.class");

            if (update.getCallbackQuery() == null) {
                String text = update.getMessage().getText();

                MySaxHandlerForButtons buttonHandlerXml = (MySaxHandlerForButtons) handler
                        .getHandler(ParserHandlerType.BUTTON);

                buttonHandlerXml.setBotType(BotType.TELEGRAM.name());

                ButtonEntityList buttonEntityList =
                        (ButtonEntityList) parser.getParser(ParserHandlerType.BUTTON)
                        .getObjectExchangeFromXML(telegramPerson.getButtonPath(), buttonHandlerXml);


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
                    telegramPerson.setCurrentStateName(state.name());
                    telegramPerson.setPrevStateName(statePrev);
                    telegramPerson.setExit(true);
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
                .getObjectExchangeFromXML(context.getTelegramPerson().getMessagePath(), messageHandlerXml);

        MessageEntity PIAR_UNIT = messageEntityList.getMessageEntities().stream()
                .filter(msg -> msg.getType().equals("PIAR_UNIT"))
                .collect(Collectors.toList()).get(0);

        if(!context.getTelegramPerson().getSubject().isEmpty()) {

            String sendText = PIAR_UNIT.getTxt() + "\n1) Назва : " +
                    context.getTelegramPerson().getMediaName() + ".\n2) Запит : " + context.getTelegramPerson().getSubject() +
                    "\n3) Посилання на таблицю запитів : " +
                    "https://docs.google.com/spreadsheets/d/1juzFkS2cZctAT7exY4N9fS4WfwrWZ3AVqweKJXAy0hE/edit#gid=0";

            SendMessage message = new SendMessage(chatIdPiarUnit, sendText);

            telegramLogger.info("TelegramIncomingMessageHandler.class : chatId = " + message.toString() +
                    ";  message = " + message.getText());

            try {

                context.getTelegramBot().execute(message);

            } catch (TelegramApiException e) {
                telegramLogger.error("TelegramIncomingMessageHandler.class ERROR : " + Arrays.asList(e.getStackTrace()));
            }
            telegramLogger.info("TelegramIncomingMessageHandler.class send message about request info to telegram of piar unit ukrposhta with chatId : " + chatIdPiarUnit);
        }
        telegramLogger.info("TelegramIncomingMessageHandler.class do not send message about request info to telegram of piar unit ukrposhta because subject of requestMedia is null.");
    }

}
