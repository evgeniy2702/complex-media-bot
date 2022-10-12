package ua.ukrposhta.complexmediabot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ua.ukrposhta.complexmediabot.bot.BotViber;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.controller.MainController;
import ua.ukrposhta.complexmediabot.model.user.PersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomTelegramMessage;
import ua.ukrposhta.complexmediabot.telegramBot.messageHandler.TelegramIncomingMessageHandler;
import ua.ukrposhta.complexmediabot.utils.TxtFileScanner;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberSender;
import ua.ukrposhta.complexmediabot.viberBot.messageHandler.ViberIncomingMessageHandler;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@ExtendWith (MockitoExtension.class)
@ContextConfiguration (classes = {
        MainController.class, TelegramIncomingMessageHandler.class, ViberIncomingMessageHandler.class, ObjectMapper.class,
        TxtFileScanner.class
})
class CacheUsersCleanerScheduleTest {

    @Spy
    private TelegramBot telegramBot;

    @MockBean
    private BotViber viberBot;

    private CacheUsersCleanerSchedule cleaner;
    private MainController controller;

    @MockBean
    private TelegramIncomingMessageHandler telegramIncomingMessageHandler;
    @MockBean
    private ViberIncomingMessageHandler viberIncomingMessageHandler;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private TxtFileScanner scanner;


    @BeforeEach
    void setUp(){
        controller = new MainController(telegramIncomingMessageHandler, viberIncomingMessageHandler, mapper, scanner);
        cleaner = new CacheUsersCleanerSchedule(telegramBot, viberBot, controller);

        Map<String, TelegramPersonEntity> telegramPersons = new HashMap<String, TelegramPersonEntity>(){{
            put("100000000", new TelegramPersonEntity(IncomTelegramMessage.builder().chat_id("100000000").build()));
            put("200000000", new TelegramPersonEntity(IncomTelegramMessage.builder().chat_id("200000000").build()));
        }};

        telegramBot.setTelegramPersons(telegramPersons);

        Mockito.verify(telegramBot).setTelegramPersons(telegramPersons);

        Map<String, ViberPersonEntity> viberPersons = new HashMap<String, ViberPersonEntity>(){{
            put("100QWERTY02", new ViberPersonEntity(ViberSender.builder().id("100QWERTY02").build()));
            put("200QWERTY02", new ViberPersonEntity(ViberSender.builder().id("200QWERTY02").build()));
        }};

        Mockito.when(viberBot.getViberPersons()).thenReturn(viberPersons);

        Map<String, PersonEntity> persons = new HashMap<String, PersonEntity>(){{
            put("100000000", new TelegramPersonEntity(IncomTelegramMessage.builder().chat_id("100000000").build()));
            put("100QWERTY02", new ViberPersonEntity(ViberSender.builder().id("100QWERTY02").build()));
            put("200000000", new TelegramPersonEntity(IncomTelegramMessage.builder().chat_id("200000000").build()));
            put("200QWERTY02", new ViberPersonEntity(ViberSender.builder().id("200QWERTY02").build()));
        }};

        controller.setPersons(persons);


        assertAll(
                ()-> assertThat(telegramBot.getTelegramPersons().size()).isNotNull().isNotZero(),
                ()-> assertThat(viberBot.getViberPersons().size()).isNotNull().isNotZero(),
                ()-> assertEquals(2, telegramBot.getTelegramPersons().size()),
                ()-> assertEquals(2, viberBot.getViberPersons().size()),
                ()-> assertThat(controller.getPersons().size()).isNotNull().isNotZero(),
                ()-> assertEquals(4, controller.getPersons().size())
        );

    }

    @Test
    void cleaner () {
        cleaner.cleaner();

        assertThat(telegramBot.getTelegramPersons().size()).isNotNull().isZero();
        assertThat(viberBot.getViberPersons().size()).isNotNull().isZero();
        assertThat(controller.getPersons().size()).isNotNull().isZero();
    }
}