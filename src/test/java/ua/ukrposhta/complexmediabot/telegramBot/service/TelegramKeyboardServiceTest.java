package ua.ukrposhta.complexmediabot.telegramBot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.bot.BotState;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonButton;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonKeyboard;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomTelegramMessage;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.ButtonType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class TelegramKeyboardServiceTest {

    private TelegramBot bot = Mockito.mock(TelegramBot.class);
    private BotContext context;
    private TelegramPersonEntity telegramPerson;
    private String incomeText = "";
    private IncomTelegramMessage incomTelegramMessage;

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
    private TelegramKeyboardService keyboardService;

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

//    TESTING OF REPLY KEYBOARD

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = END and dependency from language")
    void getCommonKeyboardReplyStateIsEnd () {
        incomTelegramMessage.setLanguageCode("uk");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.END.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);

        assertAll(
                ()->assertEquals(1, keyboardUA.size()),
                ()->assertEquals(ButtonType.START.name(), keyboardUA.getRowList().get(0).getButtonList().iterator().next().getText())
        );

        incomTelegramMessage.setLanguageCode("en");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.END.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        CommonKeyboard keyboardEN = keyboardService.getCommonKeyboardReply(context);

        assertAll(
                ()->assertEquals(1, keyboardEN.size()),
                ()->assertEquals(ButtonType.START.name(), keyboardEN.getRowList().get(0).getButtonList().iterator().next().getText())
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = SELECT and dependency from language")
    void getCommonKeyboardReplyStateIsSelect () {
        incomTelegramMessage.setLanguageCode("uk");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(pathXmlUnknownButton);
        telegramPerson.setMessagePath(pathXmlUnknownMessage);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.SELECT.name());
        telegramPerson.setPrevStateName(BotState.LANGUAGE.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        List<String> btnNameList = new ArrayList<>();

        CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);
        Iterator<CommonButton> iterator = keyboardUA.getRowList().get(0).getButtonList().iterator();
        while (true) {
            if (iterator.hasNext()) {
                btnNameList.add(iterator.next().getText());
            } else
                break;
        }

        assertAll(
                ()->assertEquals(2, keyboardUA.size()),
                ()->assertEquals(ButtonType.UA.getText(), btnNameList.get(0)),
                ()->assertEquals(ButtonType.EN.getText(), btnNameList.get(1)),
                ()->assertEquals(ButtonType.END_WORK.getText(), keyboardUA.getRowList().get(1).getButtonList().iterator().next().getText())
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = GOOD_DAY and dependency from language and activity = false")
    void getCommonKeyboardReplyStateIsGoodDayIfActivityIsFalse () {
        incomTelegramMessage.setLanguageCode("uk");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        telegramPerson.setPrevStateName(BotState.START.name());
        telegramPerson.setActivity(false);
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        List<String> btnNameList = new ArrayList<>();

        CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);

        Iterator<CommonButton> iterator = keyboardUA.getRowList().get(0).getButtonList().iterator();
        while (true) {
            if (iterator.hasNext()) {
                btnNameList.add(iterator.next().getText());
            } else
                break;
        }

        assertAll(
                ()->assertEquals(1, keyboardUA.size()),
                ()->assertEquals("Подати запит.", btnNameList.get(0)),
                ()->assertEquals("Закінчити роботу з ботом.", btnNameList.get(1))
        );

        incomTelegramMessage.setLanguageCode("en");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        telegramPerson.setPrevStateName(BotState.START.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);


        CommonKeyboard keyboardEN = keyboardService.getCommonKeyboardReply(context);

        iterator = keyboardEN.getRowList().get(0).getButtonList().iterator();
        while (true) {
            if (iterator.hasNext()) {
                btnNameList.add(iterator.next().getText());
            } else
                break;
        }

        assertAll(
                ()->assertEquals(1, keyboardEN.size()),
                ()->assertEquals("Submit a request.", btnNameList.get(2)),
                ()->assertEquals("Finish working with the bot.", btnNameList.get(3))
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = GOOD_DAY and dependency from language and activity = true")
    void getCommonKeyboardReplyStateIsGoodDayIfActivityIsTrue () {
        incomTelegramMessage.setLanguageCode("uk");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        telegramPerson.setPrevStateName(BotState.START.name());
        telegramPerson.setActivity(true);
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        List<String> btnNameList = new ArrayList<>();

        CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);

        Iterator<CommonButton> iterator = keyboardUA.getRowList().get(0).getButtonList().iterator();
        while (true) {
            if (iterator.hasNext()) {
                btnNameList.add(iterator.next().getText());
            } else
                break;
        }

        assertAll(
                ()->assertEquals(1, keyboardUA.size()),
                ()->assertEquals("Розпочати новий запит.", btnNameList.get(0)),
                ()->assertEquals("Закінчити роботу з ботом.", btnNameList.get(1))
        );

        incomTelegramMessage.setLanguageCode("en");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.GOOD_DAY.name());
        telegramPerson.setPrevStateName(BotState.START.name());
        telegramPerson.setActivity(true);
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);


        CommonKeyboard keyboardEN = keyboardService.getCommonKeyboardReply(context);

        iterator = keyboardEN.getRowList().get(0).getButtonList().iterator();
        while (true) {
            if (iterator.hasNext()) {
                btnNameList.add(iterator.next().getText());
            } else
                break;
        }

        assertAll(
                ()->assertEquals(1, keyboardEN.size()),
                ()->assertEquals("Start a new query.", btnNameList.get(2)),
                ()->assertEquals("Finish working with the bot.", btnNameList.get(3))
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state = MEDIA and dependency from language")
    void getCommonKeyboardReplyStateIsMedia () {
        incomTelegramMessage.setLanguageCode("uk");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.MEDIA.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());
        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        List<String> btnNameList = new ArrayList<>();

        CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);

        Iterator<CommonButton> iterator = keyboardUA.getRowList().get(0).getButtonList().iterator();
        while (true) {
            if (iterator.hasNext()) {
                btnNameList.add(iterator.next().getText());
            } else
                break;
        }

        assertAll(
                ()->assertEquals(1, keyboardUA.size()),
                ()->assertEquals("Розпочати новий запит.", btnNameList.get(0)),
                ()->assertEquals("Закінчити роботу з ботом.", btnNameList.get(1))
        );

        incomTelegramMessage.setLanguageCode("en");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.MEDIA.name());
        telegramPerson.setPrevStateName(BotState.GOOD_DAY.name());

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        CommonKeyboard keyboardEN = keyboardService.getCommonKeyboardReply(context);

        iterator = keyboardEN.getRowList().get(0).getButtonList().iterator();
        while (true) {
            if (iterator.hasNext()) {
                btnNameList.add(iterator.next().getText());
            } else
                break;
        }

        assertAll(
                ()->assertEquals(1, keyboardEN.size()),
                ()->assertEquals("Start a new query.", btnNameList.get(2)),
                ()->assertEquals("Finish working with the bot.", btnNameList.get(3))
        );
    }

    @Test
    @DisplayName("Testing getCommonKeyboardReply() method with state not equals END, SELECT, GOOD_DAY, MEDIA and dependency from language")
    void getCommonKeyboardReplyStateIsOther () {
        incomTelegramMessage.setLanguageCode("uk");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        for(BotState state : BotState.values()){
            if(!Arrays.asList(BotState.END, BotState.SELECT, BotState.GOOD_DAY, BotState.MEDIA).contains(state)){
                telegramPerson.setCurrentStateName(state.name());

                context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

                CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);

                assertAll(
                        ()->assertEquals(1, keyboardUA.size()),
                        ()->assertEquals("Закінчити роботу з ботом.", keyboardUA.getRowList().get(0).iterator().next().getText())
                );
            }
        }

        incomTelegramMessage.setLanguageCode("en");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        for(BotState state : BotState.values()){
            if(!Arrays.asList(BotState.END, BotState.SELECT, BotState.GOOD_DAY, BotState.MEDIA).contains(state)){
                telegramPerson.setCurrentStateName(state.name());

                context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

                CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);

                assertAll(
                        ()->assertEquals(1, keyboardUA.size()),
                        ()->assertEquals("Finish working with the bot.", keyboardUA.getRowList().get(0).iterator().next().getText())
                );
            }
        }

        incomTelegramMessage.setLanguageCode("unknown");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(pathXmlUnknownButton);
        telegramPerson.setMessagePath(pathXmlUnknownMessage);
        telegramPerson.setErrorPath(errorsPathEN);
        for(BotState state : BotState.values()){
            if(!Arrays.asList(BotState.END, BotState.SELECT, BotState.GOOD_DAY, BotState.MEDIA).contains(state)){
                telegramPerson.setCurrentStateName(state.name());

                context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

                CommonKeyboard keyboardUA = keyboardService.getCommonKeyboardReply(context);

                assertAll(
                        ()->assertEquals(1, keyboardUA.size()),
                        ()->assertEquals("Закінчити роботу з ботом | Finish working with the bot.", keyboardUA.getRowList().get(0).iterator().next().getText())
                );
            }
        }
    }



//    TESTING OF INLINE KEYBOARD
    @Test
    @DisplayName("Testing getCommonKeyboardInline() method with state = PHONE and dependency from language")
    void getCommonKeyboardInlineStateIsPhone () {
        incomTelegramMessage.setLanguageCode("uk");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathUA);
        telegramPerson.setMessagePath(messagesPathUA);
        telegramPerson.setErrorPath(errorsPathUA);
        telegramPerson.setCurrentStateName(BotState.PHONE.name());
        telegramPerson.setPrevStateName(BotState.NAME_SURNAME.name());

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        InlineKeyboardMarkup keyboardUA = keyboardService.getCommonKeyboardInline(context);

        assertAll(
                ()->assertEquals(1, keyboardUA.getKeyboard().get(0).size()),
                ()->assertEquals("Вставити зареєстровані:  Winter Bob ?", keyboardUA.getKeyboard().get(0).get(0).getText())
        );

        incomTelegramMessage.setLanguageCode("en");

        telegramPerson = new TelegramPersonEntity(incomTelegramMessage);
        telegramPerson.setButtonPath(buttonsPathEN);
        telegramPerson.setMessagePath(messagesPathEN);
        telegramPerson.setErrorPath(errorsPathEN);
        telegramPerson.setCurrentStateName(BotState.PHONE.name());
        telegramPerson.setPrevStateName(BotState.NAME_SURNAME.name());

        context = BotContext.ofTelegram(BotType.TELEGRAM, bot, telegramPerson, incomeText);

        InlineKeyboardMarkup keyboardEN = keyboardService.getCommonKeyboardInline(context);

        assertAll(
                ()->assertEquals(1, keyboardEN.getKeyboard().get(0).size()),
                ()->assertEquals("Paste your registered :  Winter Bob ?", keyboardEN.getKeyboard().get(0).get(0).getText())
        );
    }
}