package ua.ukrposhta.complexmediabot.viberBot.service;

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
import ua.ukrposhta.complexmediabot.viberBot.keyboard.ViberKeyboard;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ViberKeyboardServiceTest {

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
    private ViberKeyboardService viberKeyboardService;
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
    @DisplayName("Testing getViberKeyboard() method according states : current = SELECT, previous = START and language = UA")
    void getViberKeyboardStateIsSelectUA () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.UA.getText();
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(pathXmlUnknownButton);
        viberPerson.setMessagePath(pathXmlUnknownMessage);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.SELECT.name());
        viberPerson.setPrevStateName(BotState.LANGUAGE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(3, keyboard.getButtons().size()),
                ()->assertEquals(ButtonType.UA.getText(), keyboard.getButtons().get(0).getText()),
                ()->assertEquals(ButtonType.EN.getText(), keyboard.getButtons().get(1).getText()),
                ()->assertEquals(ButtonType.END_WORK.getText(), keyboard.getButtons().get(2).getText())
        );
    }

    @Test
    @DisplayName("Testing getViberKeyboard() method according states : current = SELECT, previous = START and language = EN")
    void getViberKeyboardStateIsSelectEN () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.EN.getText();
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(pathXmlUnknownButton);
        viberPerson.setMessagePath(pathXmlUnknownMessage);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.SELECT.name());
        viberPerson.setPrevStateName(BotState.LANGUAGE.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(3, keyboard.getButtons().size()),
                ()->assertEquals(ButtonType.UA.getText(), keyboard.getButtons().get(0).getText()),
                ()->assertEquals(ButtonType.EN.getText(), keyboard.getButtons().get(1).getText()),
                ()->assertEquals(ButtonType.END_WORK.getText(), keyboard.getButtons().get(2).getText())
        );
    }

    @Test
    @DisplayName("Testing getViberKeyboard() method according states : current = END, previous = GOOD_DAY and language = UA")
    void getViberKeyboardStateIsEndUA() throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.UA.getText();
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.END.name());
        viberPerson.setPrevStateName(BotState.GOOD_DAY.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(1, keyboard.getButtons().size()),
                ()->assertEquals(ButtonType.START.name(), keyboard.getButtons().get(0).getText())
        );
    }

    @Test
    @DisplayName("Testing getViberKeyboard() method according states : current = END, previous = GOOD_DAY and language = EN")
    void getViberKeyboardStateIsEndEN() throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.EN.getText();
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.END.name());
        viberPerson.setPrevStateName(BotState.GOOD_DAY.name());

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(1, keyboard.getButtons().size()),
                ()->assertEquals(ButtonType.START.name(), keyboard.getButtons().get(0).getText())
        );
    }

    @Test
    @DisplayName("Testing getViberKeyboard() method according states : current = MEDIA, previous = GOOD_DAY and language = UA")
    void getViberKeyboardStateIsMediaUA() throws Exception {
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

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(2, keyboard.getButtons().size()),
                ()->assertEquals("Розпочати новий запит.", keyboard.getButtons().get(0).getText()),
                ()->assertEquals("Закінчити роботу з ботом.", keyboard.getButtons().get(1).getText())
        );
    }

    @Test
    @DisplayName("Testing getViberKeyboard() method according states : current = MEDIA, previous = GOOD_DAY and language = EN")
    void getViberKeyboardStateIsMediaEN() throws Exception {
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

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(2, keyboard.getButtons().size()),
                ()->assertEquals("Start a new query.", keyboard.getButtons().get(0).getText()),
                ()->assertEquals("Finish working with the bot.", keyboard.getButtons().get(1).getText())
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = GOOD_DAY , language = UA and activity = false")
    void getViberKeyboardStateIsGoodDayIfActivityIsFalseUA () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.UA.getText();
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        viberPerson.setPrevStateName(BotState.START.name());
        viberPerson.setActivity(false);

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(2, keyboard.getButtons().size()),
                ()->assertEquals("Подати запит.", keyboard.getButtons().get(0).getText()),
                ()->assertEquals("Закінчити роботу з ботом.", keyboard.getButtons().get(1).getText())
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = GOOD_DAY , language = UA and activity = true")
    void getViberKeyboardStateIsGoodDayIfActivityIsTrueUA () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.UA.getText();
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        viberPerson.setPrevStateName(BotState.START.name());
        viberPerson.setActivity(true);

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(2, keyboard.getButtons().size()),
                ()->assertEquals("Розпочати новий запит.", keyboard.getButtons().get(0).getText()),
                ()->assertEquals("Закінчити роботу з ботом.", keyboard.getButtons().get(1).getText())
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = GOOD_DAY , language = EN and activity = false")
    void getViberKeyboardStateIsGoodDayIfActivityIsFalseEN () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.EN.getText();
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        viberPerson.setPrevStateName(BotState.START.name());
        viberPerson.setActivity(false);

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(2, keyboard.getButtons().size()),
                ()->assertEquals("Submit a request.", keyboard.getButtons().get(0).getText()),
                ()->assertEquals("Finish working with the bot.", keyboard.getButtons().get(1).getText())
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = GOOD_DAY , language = EN and activity = true")
    void getViberKeyboardStateIsGoodDayIfActivityIsTrueEN () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = ButtonType.EN.getText();
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        viberPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        viberPerson.setPrevStateName(BotState.START.name());
        viberPerson.setActivity(true);

        context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

        ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

        assertAll(
                ()->assertEquals(2, keyboard.getButtons().size()),
                ()->assertEquals("Start a new query.", keyboard.getButtons().get(0).getText()),
                ()->assertEquals("Finish working with the bot.", keyboard.getButtons().get(1).getText())
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state not equals END, SELECT, GOOD_DAY, MEDIA and language = UA")
    void getViberKeyboardStateIsStateIsOtherUA () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = "Some text";
        viberSender.setLanguage("uk-UA");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathUA);
        viberPerson.setMessagePath(messagesPathUA);
        viberPerson.setErrorPath(errorsPathUA);
        for(BotState state : BotState.values()) {
            if (!Arrays.asList(BotState.END, BotState.SELECT, BotState.GOOD_DAY, BotState.MEDIA).contains(state)) {
                viberPerson.setCurrentStateName(state.name());

                context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

                ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

                assertAll(
                        () -> assertEquals(1, keyboard.getButtons().size()),
                        () -> assertEquals("Закінчити роботу з ботом.", keyboard.getButtons().get(0).getText())
                );
            }
        }
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state not equals END, SELECT, GOOD_DAY, MEDIA and language = EN")
    void getViberKeyboardStateIsStateIsOtherEN () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = "Some text";
        viberSender.setLanguage("en-EN");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(buttonsPathEN);
        viberPerson.setMessagePath(messagesPathEN);
        viberPerson.setErrorPath(errorsPathEN);
        for(BotState state : BotState.values()) {
            if (!Arrays.asList(BotState.END, BotState.SELECT, BotState.GOOD_DAY, BotState.MEDIA).contains(state)) {
                viberPerson.setCurrentStateName(state.name());

                context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

                ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

                assertAll(
                        () -> assertEquals(1, keyboard.getButtons().size()),
                        () -> assertEquals("Finish working with the bot.", keyboard.getButtons().get(0).getText())
                );
            }
        }
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state not equals END, SELECT, GOOD_DAY, MEDIA and language = unknown")
    void getViberKeyboardStateIsStateIsOtherUnknown () throws Exception {
        event = objectMapper.readTree(incomeJson.getBytes(StandardCharsets.UTF_8));

        incomeText = "Some text";
        viberSender.setLanguage("unknown");
        viberPerson = new ViberPersonEntity(viberSender);
        viberPerson.setButtonPath(pathXmlUnknownButton);
        viberPerson.setMessagePath(pathXmlUnknownMessage);
        viberPerson.setErrorPath(errorsPathEN);
        for(BotState state : BotState.values()) {
            if (!Arrays.asList(BotState.END, BotState.SELECT, BotState.GOOD_DAY, BotState.MEDIA).contains(state)) {
                viberPerson.setCurrentStateName(state.name());

                context = BotContext.ofViber(BotType.VIBER, bot, viberPerson, incomeText);

                ViberKeyboard keyboard = viberKeyboardService.getViberKeyboard(context);

                assertAll(
                        () -> assertEquals(1, keyboard.getButtons().size()),
                        () -> assertEquals("Закінчити роботу з ботом | Finish working with the bot.", keyboard.getButtons().get(0).getText())
                );
            }
        }
    }
}