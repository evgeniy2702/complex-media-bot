package ua.ukrposhta.complexmediabot.viberBot.messageHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.BotViber;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberSender;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ViberBotStateHandlerTest {

    private BotViber bot = Mockito.mock(BotViber.class);
    private BotContext context;
    private ViberPersonEntity viberPerson;
    private String incomeText = "";
    private String incomeJson = "{\n" +
            "   \"event\":\"message\",\n" +
            "   \"timestamp\":1457764197627,\n" +
            "   \"message_token\":4912661846655238145,\n" +
            "   \"sender\":{\n" +
            "      \"id\":\"01234567890A=\",\n" +
            "      \"name\":\"John McClane\",\n" +
            "      \"avatar\":\"http://avatar.example.com\",\n" +
            "      \"country\":\"UK\",\n" +
            "      \"language\":\"en\",\n" +
            "      \"api_version\":1\n" +
            "   },\n" +
            "   \"message\":{\n" +
            "      \"type\":\"text\",\n" +
            "      \"text\":\"a message to the service\",\n" +
            "      \"media\":\"http://example.com\",\n" +
            "      \"location\":{\n" +
            "         \"lat\":50.76891,\n" +
            "         \"lon\":6.11499\n" +
            "      },\n" +
            "      \"tracking_data\":\"tracking data\"\n" +
            "   }\n" +
            "}";
    private ViberSender viberSender;
    private JsonNode event;

    @Value ("${path.xml.unknown.message}")
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
    private ViberBotStateHandler viberBotStateHandler;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        viberSender = ViberSender.builder()
                .id("QWERTYU")
                .name("Example")
                .avatar("path to avatar")
                .build();
    }


    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = GOOD_DAY, previous = START and language = UA")
    void handleIncomingMessageStateIsGood_DayLangUA () throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        viberPerson.setPrevStateName(BotState.START.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                ()->assertEquals(BotState.MEDIA, state),
                ()->assertEquals(BotState.MEDIA.name(),context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = GOOD_DAY, previous = START and language = EN")
    void handleIncomingMessageStateIsGood_DayLangEN () throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        viberPerson.setPrevStateName(BotState.START.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                ()->assertEquals(BotState.MEDIA, state),
                ()->assertEquals(BotState.MEDIA.name(),context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = LANGUAGE, previous = LANGUAGE and dependency language")
    void handleIncomingMessageStateIsLanguage() throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("unknown");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(pathXmlUnknownButton);
        viberPerson.setMessagePath(pathXmlUnknownMessage);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.LANGUAGE.name());
        viberPerson.setPrevStateName(BotState.LANGUAGE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                ()->assertEquals(BotState.SELECT, state),
                ()->assertEquals(BotState.SELECT.name(),context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SELECT, previous = LANGUAGE and language = UA")
    void handleIncomingMessageStateIsSelectLangUA() throws IOException  {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.UA.getText();
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.SELECT.name());
        viberPerson.setPrevStateName(BotState.LANGUAGE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, state),
                () -> assertEquals(BotState.MEDIA.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SELECT, previous = LANGUAGE and language = UA with ERROR")
    void handleIncomingMessageStateIsSelectLangUAError() throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = "Need send 'Україньска' but send another text";
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(pathXmlUnknownButton);
        viberPerson.setMessagePath(pathXmlUnknownMessage);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.SELECT.name());
        viberPerson.setPrevStateName(BotState.LANGUAGE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.SELECT, state),
                () -> assertEquals(BotState.SELECT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SELECT, previous = LANGUAGE and language = EN")
    void handleIncomingMessageStateIsSelectLangEN() throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.EN.getText();
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.SELECT.name());
        viberPerson.setPrevStateName(BotState.LANGUAGE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, state),
                () -> assertEquals(BotState.MEDIA.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SELECT, previous = LANGUAGE and language = EN with ERROR")
    void handleIncomingMessageStateIsSelectLangENError()throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = "Need send 'English' but send another text";
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(pathXmlUnknownButton);
        viberPerson.setMessagePath(pathXmlUnknownMessage);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.SELECT.name());
        viberPerson.setPrevStateName(BotState.LANGUAGE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.SELECT, state),
                () -> assertEquals(BotState.SELECT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = MEDIA, previous = GOOD_DAY and  language = UA")
    void handleIncomingMessageStateIsMediaUA()throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.UA.getText();
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.MEDIA.name());
        viberPerson.setPrevStateName(BotState.GOOD_DAY.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.NAME_SURNAME, state),
                () -> assertEquals(BotState.NAME_SURNAME.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = MEDIA, previous = GOOD_DAY and  language = UA with ERROR")
    void handleIncomingMessageStateIsMediaUAError()throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = "Need send 'Україньска' but send another text";
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.MEDIA.name());
        viberPerson.setPrevStateName(BotState.GOOD_DAY.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, state),
                () -> assertEquals(BotState.MEDIA.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = MEDIA, previous = GOOD_DAY and  language = EN")
    void handleIncomingMessageStateIsMediaEN()throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.EN.getText();
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.MEDIA.name());
        viberPerson.setPrevStateName(BotState.GOOD_DAY.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.NAME_SURNAME, state),
                () -> assertEquals(BotState.NAME_SURNAME.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = MEDIA, previous = GOOD_DAY and  language = EN with ERROR")
    void handleIncomingMessageStateIsMediaENError()throws IOException{
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = "Need send 'English' but send another text";
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.MEDIA.name());
        viberPerson.setPrevStateName(BotState.GOOD_DAY.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.MEDIA, state),
                () -> assertEquals(BotState.MEDIA.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = NAME_SURNAME, previous = MEDIA and language = UA")
    void handleIncomingMessageStateIsName_SurnameLangUA () throws IOException{
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.NAME_SURNAME.name());
        viberPerson.setPrevStateName(BotState.MEDIA.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.PHONE, state),
                () -> assertEquals(BotState.PHONE.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = NAME_SURNAME, previous = MEDIA and language = EN")
    void handleIncomingMessageStateIsName_SurnameLangEN () throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.NAME_SURNAME.name());
        viberPerson.setPrevStateName(BotState.MEDIA.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.PHONE, state),
                () -> assertEquals(BotState.PHONE.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = PHONE, previous = NAME_SURNAME and language with UA")
    void handleIncomingMessageStateIsPhoneUA () throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.PHONE.name());
        viberPerson.setPrevStateName(BotState.NAME_SURNAME.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, state),
                () -> assertEquals(BotState.EMAIL.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = PHONE, previous = NAME_SURNAME and language with EN")
    void handleIncomingMessageStateIsPhoneEN () throws IOException {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.PHONE.name());
        viberPerson.setPrevStateName(BotState.NAME_SURNAME.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, state),
                () -> assertEquals(BotState.EMAIL.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = EMAIL, previous = PHONE and language = UA")
    void handleIncomingMessageStateIsEmailUA () throws Exception{
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"+38067 111 22 33\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.EMAIL.name());
        viberPerson.setPrevStateName(BotState.PHONE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, state),
                () -> assertEquals(BotState.SUBJECT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = EMAIL, previous = PHONE and language = UA with ERROR")
    void handleIncomingMessageStateIsEmailUAError () throws Exception{
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"067 111 22 33\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.EMAIL.name());
        viberPerson.setPrevStateName(BotState.PHONE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, state),
                () -> assertEquals(BotState.EMAIL.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = EMAIL, previous = PHONE and language = EN")
    void handleIncomingMessageStateIsEmailEN () throws Exception{
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"+38067 111 22 33\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.EMAIL.name());
        viberPerson.setPrevStateName(BotState.PHONE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, state),
                () -> assertEquals(BotState.SUBJECT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = EMAIL, previous = PHONE and language = EN with ERROR")
    void handleIncomingMessageStateIsEmailENError () throws Exception{
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"+38067-111-22-33\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.EMAIL.name());
        viberPerson.setPrevStateName(BotState.PHONE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.EMAIL, state),
                () -> assertEquals(BotState.EMAIL.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SUBJECT, previous = EMAIL and language = UA")
    void handleIncomingMessageStateIsSubjectUA () throws Exception {
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"email@gmail.com\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.SUBJECT.name());
        viberPerson.setPrevStateName(BotState.EMAIL.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.WE_CONTACT, state),
                () -> assertEquals(BotState.WE_CONTACT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SUBJECT, previous = EMAIL and language = UA with ERROR")
    void handleIncomingMessageStateIsSubjectUAError () throws Exception {
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"email#gmail.com\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.SUBJECT.name());
        viberPerson.setPrevStateName(BotState.EMAIL.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, state),
                () -> assertEquals(BotState.SUBJECT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SUBJECT, previous = EMAIL and language = EN")
    void handleIncomingMessageStateIsSubjectEN () throws Exception {
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"email@gmail.com\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.SUBJECT.name());
        viberPerson.setPrevStateName(BotState.EMAIL.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.WE_CONTACT, state),
                () -> assertEquals(BotState.WE_CONTACT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = SUBJECT, previous = EMAIL and language = EN with ERROR")
    void handleIncomingMessageStateIsSubjectENError () throws Exception {
        incomeJson = "{\n" +
                "   \"event\":\"message\",\n" +
                "   \"timestamp\":1457764197627,\n" +
                "   \"message_token\":4912661846655238145,\n" +
                "   \"sender\":{\n" +
                "      \"id\":\"01234567890A=\",\n" +
                "      \"name\":\"John McClane\",\n" +
                "      \"avatar\":\"http://avatar.example.com\",\n" +
                "      \"country\":\"UK\",\n" +
                "      \"language\":\"en\",\n" +
                "      \"api_version\":1\n" +
                "   },\n" +
                "   \"message\":{\n" +
                "      \"type\":\"text\",\n" +
                "      \"text\":\"email@gmailcom\",\n" +
                "      \"media\":\"http://example.com\",\n" +
                "      \"location\":{\n" +
                "         \"lat\":50.76891,\n" +
                "         \"lon\":6.11499\n" +
                "      },\n" +
                "      \"tracking_data\":\"tracking data\"\n" +
                "   }\n" +
                "}";

        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.SUBJECT.name());
        viberPerson.setPrevStateName(BotState.EMAIL.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.SUBJECT, state),
                () -> assertEquals(BotState.SUBJECT.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = WE_CONTACT, previous = SUBJECT and language = UA")
    void handleIncomingMessageStateIsWeContactUA () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.WE_CONTACT.name());
        viberPerson.setPrevStateName(BotState.SUBJECT.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.END, state),
                () -> assertEquals(BotState.END.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = WE_CONTACT, previous = SUBJECT and language = EN")
    void handleIncomingMessageStateIsWeContactEN () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.WE_CONTACT.name());
        viberPerson.setPrevStateName(BotState.SUBJECT.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.END, state),
                () -> assertEquals(BotState.END.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = END, previous = WE_CONTACT and language = UA")
    void handleIncomingMessageStateIsEnd () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.END.name());
        viberPerson.setPrevStateName(BotState.WE_CONTACT.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.GOOD_DAY, state),
                () -> assertEquals(BotState.GOOD_DAY.name(), context.getViberPerson().getCurrentStateName())
        );
    }

    @Test
    @DisplayName("Testing handleIncomingMessage() method according states : current = END, previous = WE_CONTACT and language = EN")
    void handleIncomingMessageStateIsEndEN () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.END.name());
        viberPerson.setPrevStateName(BotState.WE_CONTACT.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        BotState state = viberBotStateHandler.handleIncomingMessage(event, context, bot);

        assertAll(
                () -> assertEquals(BotState.GOOD_DAY, state),
                () -> assertEquals(BotState.GOOD_DAY.name(), context.getViberPerson().getCurrentStateName())
        );
    }
}