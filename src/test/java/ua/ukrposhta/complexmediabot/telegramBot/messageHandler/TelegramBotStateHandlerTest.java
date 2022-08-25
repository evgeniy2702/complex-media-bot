package ua.ukrposhta.complexmediabot.telegramBot.messageHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomTelegramMessage;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TelegramBotStateHandlerTest {

    private TelegramBot bot = Mockito.mock(TelegramBot.class);
    private BotContext context;
    private TelegramPersonEntity telegramPerson;
    private String incomeText = "";
    private IncomTelegramMessage incomTelegramMessage;
    private Update update = new Update();
    private Message message = new Message();
    private Chat chat = new Chat();
    private CallbackQuery query = new CallbackQuery();

    @Value("${path.xml.unknown.message}")
    private String pathXmlUnknownMessage;
    @Value("${path.xml.unknown.button}")
    private String pathXmlUnknownButton;

    @Value("${path.xml.ukraine.message}")
    private String messagesPathUA;
    @Value("${path.xml.ukraine.button}")
    private String buttonsPathUA;
    @Value("${path.xml.ukraine.error}")
    private String errorsPathUA;

    @Value("${path.xml.english.message}")
    private String messagesPathEN;
    @Value("${path.xml.english.button}")
    private String buttonsPathEN;
    @Value("${path.xml.english.error}")
    private String errorsPathEN;

    @Autowired
    private TelegramBotStateHandler telegramBotStateHandler;

    @BeforeEach
    void setUp(){
        incomTelegramMessage = IncomTelegramMessage.builder()
                .text(incomeText)
                .chat_id("100000000")
                .isBot(true)
                .firstName("Bob")
                .lastName("Winter")
                .userName("Bean")
                .build();
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = GOOD_DAY, previous = START and dependency language")
    void handleIncomingMessageStateIsGood_Day () {
        incomTelegramMessage.setLanguageCode("uk");
        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        telegramPerson.setPrevStateName(BotState.START.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                ()->assertEquals(BotState.MEDIA, stateUA),
                ()->assertEquals(BotState.MEDIA.name(),context.getTelegramPerson().getCurrentStateName())
        );

        incomTelegramMessage.setLanguageCode("en");
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        telegramPerson.setPrevStateName(BotState.START.name());

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                ()->assertEquals(BotState.MEDIA, stateEN),
                ()->assertEquals(BotState.MEDIA.name(),context.getTelegramPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = GOOD_DAY, previous = LANGUAGE and dependency language")
    void handleIncomingMessageStateIsLanguage () {
        incomTelegramMessage.setLanguageCode("unknown");
        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(pathXmlUnknownButton);
        telegramPerson.setMessagePath(pathXmlUnknownMessage);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.LANGUAGE.name());
        telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                ()->assertEquals(BotState.SELECT, stateUA),
                ()->assertEquals(BotState.SELECT.name(),context.getTelegramPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SELECT, previous = LANGUAGE and dependency language")
    void handleIncomingMessageStateIsSelect () {
        incomeText = ButtonType.UA.getText();
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);
        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.SELECT.name());
        telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, stateUA),
                () -> assertEquals(BotState.MEDIA.name(), context.getTelegramPerson().getCurrentStateName())
        );

        incomeText = ButtonType.EN.getText();
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.SELECT.name());
        telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, stateEN),
                () -> assertEquals(BotState.MEDIA.name(), context.getTelegramPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SELECT, previous = LANGUAGE and dependency language for " +
            "get ERROR incomTelegramMessage")
    void handleIncomingMessageStateIsSelectError () {
        incomeText = "Need send 'Україньска' but send another text";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);
        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(pathXmlUnknownButton);
        telegramPerson.setMessagePath(pathXmlUnknownMessage);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.SELECT.name());
        telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.SELECT, stateUA),
                () -> assertEquals(BotState.SELECT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "Need send 'English' but send another text";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);
        telegramPerson.setButtonPath(pathXmlUnknownButton);
        telegramPerson.setMessagePath(pathXmlUnknownMessage);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.SELECT.name());
        telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.SELECT, stateEN),
                () -> assertEquals(BotState.SELECT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = MEDIA, previous = GOOD_DAY and dependency language")
    void handleIncomingMessageStateIsMedia () {
        incomeText = ButtonType.UA.getText();
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);
        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.MEDIA.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.NAME_SURNAME, stateUA),
                () -> assertEquals(BotState.NAME_SURNAME.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = ButtonType.EN.getText();
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.MEDIA.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.NAME_SURNAME, stateEN),
                () -> assertEquals(BotState.NAME_SURNAME.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = MEDIA, previous = GOOD_DAY and dependency language for " +
            "getERROR incomTelegramMessage")
    void handleIncomingMessageStateIsMediaError () {
        incomeText = "Need send 'Україньска' but send another text";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);
        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.MEDIA.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, stateUA),
                () -> assertEquals(BotState.MEDIA.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "Need send 'English' but send another text";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.MEDIA.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(new Update(), context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, stateEN),
                () -> assertEquals(BotState.MEDIA.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = NAME_SURNAME, previous = MEDIA and dependency language")
    void handleIncomingMessageStateIsNameSurname () {
        incomeText = "Назва ЗМІ";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.NAME_SURNAME.name());
        telegramPerson.setPrevStateName(BotState.MEDIA.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.PHONE, stateUA),
                () -> assertEquals(BotState.PHONE.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "Name Media";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.NAME_SURNAME.name());
        telegramPerson.setPrevStateName(BotState.MEDIA.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.PHONE, stateEN),
                () -> assertEquals(BotState.PHONE.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = PHONE, previous = NAME_SURNAME and dependency language with " +
            "callbackquery variant for  value of Name and Surname")
    void handleIncomingMessageStateIsPhone () {
        incomeText = "Ім'я Прізвище";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.PHONE.name());
        telegramPerson.setPrevStateName(BotState.NAME_SURNAME.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, stateUA),
                () -> assertEquals(BotState.EMAIL.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "Name Surname";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.PHONE.name());
        telegramPerson.setPrevStateName(BotState.NAME_SURNAME.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, stateEN),
                () -> assertEquals(BotState.EMAIL.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        query.setData(incomeText);
        update.setCallbackQuery(query);

        BotState stateCallbackquery = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, stateCallbackquery),
                () -> assertEquals(BotState.EMAIL.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = EMAIL, previous = PHONE and dependency language")
    void handleIncomingMessageStateIsEmail () {
        incomeText = "+38067 111 22 33";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.EMAIL.name());
        telegramPerson.setPrevStateName(BotState.PHONE.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, stateUA),
                () -> assertEquals(BotState.SUBJECT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "+38067 111 22 33";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.EMAIL.name());
        telegramPerson.setPrevStateName(BotState.PHONE.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, stateEN),
                () -> assertEquals(BotState.SUBJECT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = EMAIL, previous = PHONE and dependency language with " +
            "error in phone number")
    void handleIncomingMessageStateIsEmailError () {
        incomeText = "+38067-111=22-33";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.EMAIL.name());
        telegramPerson.setPrevStateName(BotState.PHONE.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, stateUA),
                () -> assertEquals(BotState.EMAIL.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "067 111 22 33";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.EMAIL.name());
        telegramPerson.setPrevStateName(BotState.PHONE.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, stateEN),
                () -> assertEquals(BotState.EMAIL.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SUBJECT, previous = EMAIL and dependency language")
    void handleIncomingMessageStateIsSubject () {
        incomeText = "email@gmail.com";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.SUBJECT.name());
        telegramPerson.setPrevStateName(BotState.EMAIL.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.WE_CONTACT, stateUA),
                () -> assertEquals(BotState.WE_CONTACT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "name.surname@www.com";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.SUBJECT.name());
        telegramPerson.setPrevStateName(BotState.EMAIL.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.WE_CONTACT, stateEN),
                () -> assertEquals(BotState.WE_CONTACT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SUBJECT, previous = EMAIL and dependency language with " +
            "error in phone number")
    void handleIncomingMessageStateIsSubjectError () {
        incomeText = "email#gmail.com";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.SUBJECT.name());
        telegramPerson.setPrevStateName(BotState.EMAIL.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, stateUA),
                () -> assertEquals(BotState.SUBJECT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "name.surname";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.SUBJECT.name());
        telegramPerson.setPrevStateName(BotState.EMAIL.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, stateEN),
                () -> assertEquals(BotState.SUBJECT.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = WE_CONTACT, previous = SUBJECT and dependency language")
    void handleIncomingMessageStateIsWeContact () {
        incomeText = "Текст запита";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.WE_CONTACT.name());
        telegramPerson.setPrevStateName(BotState.SUBJECT.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.END, stateUA),
                () -> assertEquals(BotState.END.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "Text of query";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.WE_CONTACT.name());
        telegramPerson.setPrevStateName(BotState.SUBJECT.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.END, stateEN),
                () -> assertEquals(BotState.END.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = END, previous = WE_CONTACT and dependency language")
    void handleIncomingMessageStateIsEnd () {
        incomeText = "";
        incomTelegramMessage.setLanguageCode("uk");
        incomTelegramMessage.setText(incomeText);

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.END.name());
        telegramPerson.setPrevStateName(BotState.WE_CONTACT.name());

        chat.setId(Long.valueOf(telegramPerson.getIncomTelegramMessage().getChat_id()));
        message.setChat(chat);
        message.setText(incomeText);
        update.setMessage(message);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateUA = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.GOOD_DAY, stateUA),
                () -> assertEquals(BotState.GOOD_DAY.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );

        incomeText = "";
        incomTelegramMessage.setLanguageCode("en");
        incomTelegramMessage.setText(incomeText);

        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.END.name());
        telegramPerson.setPrevStateName(BotState.WE_CONTACT.name());

        message.setText(incomeText);

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        BotState stateEN = telegramBotStateHandler.handleIncomingMessage(update, context, bot);

        assertAll(
                () -> assertEquals(BotState.GOOD_DAY, stateEN),
                () -> assertEquals(BotState.GOOD_DAY.name(), context.getTelegramPerson().getCurrentStateName()),
                () -> assertEquals(incomeText, context.getInput())
        );
    }
}