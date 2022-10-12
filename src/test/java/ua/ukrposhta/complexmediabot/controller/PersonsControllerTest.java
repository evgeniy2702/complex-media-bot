package ua.ukrposhta.complexmediabot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import ua.ukrposhta.complexmediabot.bot.BotViber;
import ua.ukrposhta.complexmediabot.bot.TelegramBot;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.telegramBot.message.IncomTelegramMessage;
import ua.ukrposhta.complexmediabot.telegramBot.messageHandler.TelegramIncomingMessageHandler;
import ua.ukrposhta.complexmediabot.utils.TxtFileScanner;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberSender;
import ua.ukrposhta.complexmediabot.viberBot.messageHandler.ViberIncomingMessageHandler;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest (controllers = {
        PersonsController.class, MainController.class, TxtFileScanner.class
})
@TestPropertySource(locations = {
        "classpath:application-test.properties"
})
@AutoConfigureMockMvc
@TestMethodOrder (OrderAnnotation.class)
class PersonsControllerTest {

    private PersonsController controller;

    @Autowired
    public MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MainController mainController;
    @Autowired
    private TxtFileScanner scanner;
    @Value("${file.path.telegram}")
    private String pathTelegram;
    @Value("${file.path.viber}")
    private String pathViber;

    @MockBean
    private TelegramIncomingMessageHandler telegramIncomingMessageHandler;
    @MockBean
    private ViberIncomingMessageHandler viberIncomingMessageHandler;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private TelegramBot telegramBot;
    @MockBean
    private BotViber viberBot;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        controller = new PersonsController(telegramBot, viberBot, mainController, scanner);

        TelegramPersonEntity first_telegram = new TelegramPersonEntity(IncomTelegramMessage.builder().chat_id("100000000").build());
        first_telegram.setName_surname("First Telegram");
        first_telegram.setMediaName("first_telegram_media");
        first_telegram.setPhone("+38067 111 11 11");
        first_telegram.setEmail("first_telegram@www.ua");
        first_telegram.setSubject("first_telegram_subject");

        TelegramPersonEntity second_telegram = new TelegramPersonEntity(IncomTelegramMessage.builder().chat_id("200000000").build());
        second_telegram.setName_surname("Second Telegram");
        second_telegram.setMediaName("second_telegram_media");
        second_telegram.setPhone("+38067 222 22 22");
        second_telegram.setEmail("second_telegram@www.ua");
        second_telegram.setSubject("second_telegram_subject");

        Map<String, TelegramPersonEntity> telegramPersons = new HashMap<String, TelegramPersonEntity>(){{
            put("100000000", first_telegram);
            put("200000000", second_telegram);
        }};

        when(telegramBot.getTelegramPersons()).thenReturn(telegramPersons);

        ViberPersonEntity first_viber = new ViberPersonEntity(ViberSender.builder().id("100QWERTY02").build());
        first_viber.setName_surname("First Viber");
        first_viber.setMediaName("first_viber_media");
        first_viber.setPhone("+38066 111 11 11");
        first_viber.setEmail("first_viber@www.ua");
        first_viber.setSubject("first_viber_subject");

        ViberPersonEntity second_viber = new ViberPersonEntity(ViberSender.builder().id("200QWERTY02").build());
        second_viber.setName_surname("Second Viber");
        second_viber.setMediaName("second_viber_media");
        second_viber.setPhone("+38066 222 22 22");
        second_viber.setEmail("second_viber@www.ua");
        second_viber.setSubject("second_viber_subject");

        Map<String, ViberPersonEntity> viberPersons = new HashMap<String, ViberPersonEntity>(){{
            put("100QWERTY02", first_viber);
            put("200QWERTY02", second_viber);
        }};

        Mockito.when(viberBot.getViberPersons()).thenReturn(viberPersons);

        controller.getController().getPersons().putAll(telegramPersons);
        controller.getController().getPersons().putAll(viberPersons);

    }

    @Test
    @DisplayName("Testing a controller method to produce an html page listing all PersonEntity that both bots use " +
            "for media")
    void personsCache () throws Exception {
        assertAll(
                ()-> assertThat(telegramBot.getTelegramPersons().size()).isNotZero(),
                ()-> assertEquals(2, telegramBot.getTelegramPersons().size()),
                ()-> assertThat(viberBot.getViberPersons().size()).isNotZero(),
                ()-> assertEquals(2, viberBot.getViberPersons().size()),
                ()-> assertThat(mainController.getPersons().size()).isNotZero(),
                ()-> assertEquals(4, mainController.getPersons().size())
        );

        mockMvc.perform(get("/cache/all-bots"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список користувачів, які зараз використовують ботів : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 2;</li>" +
                                "<li>в ВАЙБЕР боті - 2;</li>" +
                                "</ol></center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ТЕЛЕГРАММ БОТ перелік юзерів : </center>" +

                                "<form id='del_telegram_100000000' action='/Complex-Media-Bot/cache/telegram/delete' method='post'>" +
                                    "<p><input name='id' form='del_telegram_100000000' value=100000000></p>" +
                                    "<p><input type='submit' form='del_telegram_100000000' value='DELETE'></p>" +
                                "</form>" +
                                    "<p>name_surname : First Telegram;</p>" +
                                    "<p>media : first_telegram_media;</p>" +
                                    "<p>phone : +38067 111 11 11;</p>" +
                                    "<p>email : first_telegram@www.ua;</p>" +
                                    "<p>subject : first_telegram_subject;</p>" +
                                "<form id='del_telegram_200000000' action='/Complex-Media-Bot/cache/telegram/delete' method='post'>" +
                                    "<p><input name='id' form='del_telegram_200000000' value=200000000></p>" +
                                    "<p><input type='submit' form='del_telegram_200000000' value='DELETE'></p>" +
                                "</form>" +
                                    "<p>name_surname : Second Telegram;</p>" +
                                    "<p>media : second_telegram_media;</p>" +
                                    "<p>phone : +38067 222 22 22;</p>" +
                                    "<p>email : second_telegram@www.ua;</p>" +
                                    "<p>subject : second_telegram_subject;</p>" +

                                "<form id='del_telegram' action='/Complex-Media-Bot/cache/telegram/delete-all' method='get'>" +
                                    "<p><input type='submit' form='del_telegram' value='DELETE ALL TELEGRAM'></p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ВАЙБЕР БОТ перелік юзерів : </center>" +

                                "<form id='del_viber_100QWERTY02' action='/Complex-Media-Bot/cache/viber/delete' method='post'>" +
                                    "<p><input name='id' form='del_viber_100QWERTY02' value=100QWERTY02></p>" +
                                    "<p><input type='submit' form='del_viber_100QWERTY02' value='DELETE'></p>" +
                                "</form>" +
                                    "<p>name_surname : First Viber;</p>" +
                                    "<p>media : first_viber_media;</p>" +
                                    "<p>phone : +38066 111 11 11;</p>" +
                                    "<p>email : first_viber@www.ua;</p>" +
                                    "<p>subject : first_viber_subject;</p>" +
                                "<form id='del_viber_200QWERTY02' action='/Complex-Media-Bot/cache/viber/delete' method='post'>" +
                                    "<p><input name='id' form='del_viber_200QWERTY02' value=200QWERTY02></p>" +
                                    "<p><input type='submit' form='del_viber_200QWERTY02' value='DELETE'></p>" +
                                "</form>" +
                                    "<p>name_surname : Second Viber;</p>" +
                                    "<p>media : second_viber_media;</p>" +
                                    "<p>phone : +38066 222 22 22;</p>" +
                                    "<p>email : second_viber@www.ua;</p>" +
                                    "<p>subject : second_viber_subject;</p>" +

                                "<form id='del_viber' action='/Complex-Media-Bot/cache/viber/delete-all' method='get'>" +
                                "<p><input type='submit' form='del_viber' value='DELETE ALL VIBER'></p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-bots' method='get'>" +
                                    "<p><input type='submit' form='del_all_bots' value='DELETE ALL BOTS'></p>" +
                                "</form>"),
                status().isOk());

        this.mockMvc.perform(post("/cache/all-bots")).andExpect(status().isMethodNotAllowed());
        this.mockMvc.perform(get("/cache/all-bot")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Testing a controller method to produce an html page listing all PersonEntity that both bots use " +
            "for media after removing all PersonEntity from the Map<String, PersonEntity> cache.")
    void deleteAllCache () throws Exception
    {

        assertAll(
                ()-> assertThat(telegramBot.getTelegramPersons().size()).isNotZero(),
                ()-> assertEquals(2, telegramBot.getTelegramPersons().size()),
                ()-> assertThat(viberBot.getViberPersons().size()).isNotZero(),
                ()-> assertEquals(2, viberBot.getViberPersons().size()),
                ()-> assertThat(mainController.getPersons().size()).isNotZero(),
                ()-> assertEquals(4, mainController.getPersons().size())
        );

        mockMvc.perform(get("/cache/delete-all-bots"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список користувачів, які зараз використовують ботів : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 0;</li>" +
                                "<li>в ВАЙБЕР боті - 0;</li>" +
                                "</ol></center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ТЕЛЕГРАММ БОТ перелік юзерів : </center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ВАЙБЕР БОТ перелік юзерів : </center>"),
                        status().isOk());

        this.mockMvc.perform(post("/cache/delete-all-bots")).andExpect(status().isMethodNotAllowed());
        this.mockMvc.perform(get("/cache/delete-all")).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Testing a controller method to produce an html page listing all PersonEntity that both bots " +
            "use for media after removing PersonEntity for Telegram bot by user_id from the Map<String, PersonEntity> cache.")
    void deleteTelegramCacheById () throws Exception {

        assertAll(
                ()-> assertThat(telegramBot.getTelegramPersons().size()).isNotZero(),
                ()-> assertEquals(2, telegramBot.getTelegramPersons().size()),
                ()-> assertThat(viberBot.getViberPersons().size()).isNotZero(),
                ()-> assertEquals(2, viberBot.getViberPersons().size()),
                ()-> assertThat(mainController.getPersons().size()).isNotZero(),
                ()-> assertEquals(4, mainController.getPersons().size())
        );

        mockMvc.perform(post("/cache/telegram/delete").param("id","100000000"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список користувачів, які зараз використовують ботів : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 1;</li>" +
                                "<li>в ВАЙБЕР боті - 2;</li>" +
                                "</ol></center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ТЕЛЕГРАММ БОТ перелік юзерів : </center>" +

                                "<form id='del_telegram_200000000' action='/Complex-Media-Bot/cache/telegram/delete' method='post'>" +
                                "<p><input name='id' form='del_telegram_200000000' value=200000000></p>" +
                                "<p><input type='submit' form='del_telegram_200000000' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : Second Telegram;</p>" +
                                "<p>media : second_telegram_media;</p>" +
                                "<p>phone : +38067 222 22 22;</p>" +
                                "<p>email : second_telegram@www.ua;</p>" +
                                "<p>subject : second_telegram_subject;</p>" +

                                "<form id='del_telegram' action='/Complex-Media-Bot/cache/telegram/delete-all' method='get'>" +
                                "<p><input type='submit' form='del_telegram' value='DELETE ALL TELEGRAM'></p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ВАЙБЕР БОТ перелік юзерів : </center>" +

                                "<form id='del_viber_100QWERTY02' action='/Complex-Media-Bot/cache/viber/delete' method='post'>" +
                                "<p><input name='id' form='del_viber_100QWERTY02' value=100QWERTY02></p>" +
                                "<p><input type='submit' form='del_viber_100QWERTY02' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : First Viber;</p>" +
                                "<p>media : first_viber_media;</p>" +
                                "<p>phone : +38066 111 11 11;</p>" +
                                "<p>email : first_viber@www.ua;</p>" +
                                "<p>subject : first_viber_subject;</p>" +
                                "<form id='del_viber_200QWERTY02' action='/Complex-Media-Bot/cache/viber/delete' method='post'>" +
                                "<p><input name='id' form='del_viber_200QWERTY02' value=200QWERTY02></p>" +
                                "<p><input type='submit' form='del_viber_200QWERTY02' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : Second Viber;</p>" +
                                "<p>media : second_viber_media;</p>" +
                                "<p>phone : +38066 222 22 22;</p>" +
                                "<p>email : second_viber@www.ua;</p>" +
                                "<p>subject : second_viber_subject;</p>" +

                                "<form id='del_viber' action='/Complex-Media-Bot/cache/viber/delete-all' method='get'>" +
                                "<p><input type='submit' form='del_viber' value='DELETE ALL VIBER'></p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-bots' method='get'>" +
                                "<p><input type='submit' form='del_all_bots' value='DELETE ALL BOTS'></p>" +
                                "</form>"),
                        status().isOk());

        this.mockMvc.perform(get("/cache/telegram/delete")).andExpect(status().isMethodNotAllowed());
        this.mockMvc.perform(post("/cache/telegram/deletes")).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Testing a controller method to produce an html page listing all PersonEntity that both bots " +
            "use for media after removing all PersonEntity for Telegram bot from the Map<String, PersonEntity> cache.")
    void deleteAllTelegramCache () throws Exception {

        assertAll(
                ()-> assertThat(telegramBot.getTelegramPersons().size()).isNotZero(),
                ()-> assertEquals(2, telegramBot.getTelegramPersons().size()),
                ()-> assertThat(viberBot.getViberPersons().size()).isNotZero(),
                ()-> assertEquals(2, viberBot.getViberPersons().size()),
                ()-> assertThat(mainController.getPersons().size()).isNotZero(),
                ()-> assertEquals(4, mainController.getPersons().size())
        );

        mockMvc.perform(get("/cache/telegram/delete-all"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список користувачів, які зараз використовують ботів : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 0;</li>" +
                                "<li>в ВАЙБЕР боті - 2;</li>" +
                                "</ol></center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ТЕЛЕГРАММ БОТ перелік юзерів : </center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ВАЙБЕР БОТ перелік юзерів : </center>" +

                                "<form id='del_viber_100QWERTY02' action='/Complex-Media-Bot/cache/viber/delete' method='post'>" +
                                "<p><input name='id' form='del_viber_100QWERTY02' value=100QWERTY02></p>" +
                                "<p><input type='submit' form='del_viber_100QWERTY02' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : First Viber;</p>" +
                                "<p>media : first_viber_media;</p>" +
                                "<p>phone : +38066 111 11 11;</p>" +
                                "<p>email : first_viber@www.ua;</p>" +
                                "<p>subject : first_viber_subject;</p>" +
                                "<form id='del_viber_200QWERTY02' action='/Complex-Media-Bot/cache/viber/delete' method='post'>" +
                                "<p><input name='id' form='del_viber_200QWERTY02' value=200QWERTY02></p>" +
                                "<p><input type='submit' form='del_viber_200QWERTY02' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : Second Viber;</p>" +
                                "<p>media : second_viber_media;</p>" +
                                "<p>phone : +38066 222 22 22;</p>" +
                                "<p>email : second_viber@www.ua;</p>" +
                                "<p>subject : second_viber_subject;</p>" +

                                "<form id='del_viber' action='/Complex-Media-Bot/cache/viber/delete-all' method='get'>" +
                                "<p><input type='submit' form='del_viber' value='DELETE ALL VIBER'></p>" +
                                "</form>"),
                        status().isOk());

        this.mockMvc.perform(post("/cache/telegram/delete-all")).andExpect(status().isMethodNotAllowed());
        this.mockMvc.perform(get("/cache/telegram/delete-alls")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Testing a controller method to produce an html page listing all PersonEntity that both bots " +
            "use for media after removing PersonEntity for Viber bot by user_id from the Map<String, PersonEntity> cache.")
    void deleteViberCacheById () throws Exception {
        assertAll(
                ()-> assertThat(telegramBot.getTelegramPersons().size()).isNotZero(),
                ()-> assertEquals(2, telegramBot.getTelegramPersons().size()),
                ()-> assertThat(viberBot.getViberPersons().size()).isNotZero(),
                ()-> assertEquals(2, viberBot.getViberPersons().size()),
                ()-> assertThat(mainController.getPersons().size()).isNotZero(),
                ()-> assertEquals(4, mainController.getPersons().size())
        );

        mockMvc.perform(post("/cache/viber/delete").param("id","100QWERTY02"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список користувачів, які зараз використовують ботів : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 2;</li>" +
                                "<li>в ВАЙБЕР боті - 1;</li>" +
                                "</ol></center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ТЕЛЕГРАММ БОТ перелік юзерів : </center>" +

                                "<form id='del_telegram_100000000' action='/Complex-Media-Bot/cache/telegram/delete' method='post'>" +
                                "<p><input name='id' form='del_telegram_100000000' value=100000000></p>" +
                                "<p><input type='submit' form='del_telegram_100000000' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : First Telegram;</p>" +
                                "<p>media : first_telegram_media;</p>" +
                                "<p>phone : +38067 111 11 11;</p>" +
                                "<p>email : first_telegram@www.ua;</p>" +
                                "<p>subject : first_telegram_subject;</p>" +
                                "<form id='del_telegram_200000000' action='/Complex-Media-Bot/cache/telegram/delete' method='post'>" +
                                "<p><input name='id' form='del_telegram_200000000' value=200000000></p>" +
                                "<p><input type='submit' form='del_telegram_200000000' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : Second Telegram;</p>" +
                                "<p>media : second_telegram_media;</p>" +
                                "<p>phone : +38067 222 22 22;</p>" +
                                "<p>email : second_telegram@www.ua;</p>" +
                                "<p>subject : second_telegram_subject;</p>" +

                                "<form id='del_telegram' action='/Complex-Media-Bot/cache/telegram/delete-all' method='get'>" +
                                "<p><input type='submit' form='del_telegram' value='DELETE ALL TELEGRAM'></p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ВАЙБЕР БОТ перелік юзерів : </center>" +

                                "<form id='del_viber_200QWERTY02' action='/Complex-Media-Bot/cache/viber/delete' method='post'>" +
                                "<p><input name='id' form='del_viber_200QWERTY02' value=200QWERTY02></p>" +
                                "<p><input type='submit' form='del_viber_200QWERTY02' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : Second Viber;</p>" +
                                "<p>media : second_viber_media;</p>" +
                                "<p>phone : +38066 222 22 22;</p>" +
                                "<p>email : second_viber@www.ua;</p>" +
                                "<p>subject : second_viber_subject;</p>" +

                                "<form id='del_viber' action='/Complex-Media-Bot/cache/viber/delete-all' method='get'>" +
                                "<p><input type='submit' form='del_viber' value='DELETE ALL VIBER'></p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-bots' method='get'>" +
                                "<p><input type='submit' form='del_all_bots' value='DELETE ALL BOTS'></p>" +
                                "</form>"),
                        status().isOk());

        this.mockMvc.perform(get("/cache/viber/delete")).andExpect(status().isMethodNotAllowed());
        this.mockMvc.perform(post("/cache/viber/deletes")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Testing a controller method to produce an html page listing all PersonEntity that both bots " +
            "use for media after removing all PersonEntity for Viber bot from the Map<String, PersonEntity> cache.")
    void deleteAllViberCache () throws Exception {
        assertAll(
                ()-> assertThat(telegramBot.getTelegramPersons().size()).isNotZero(),
                ()-> assertEquals(2, telegramBot.getTelegramPersons().size()),
                ()-> assertThat(viberBot.getViberPersons().size()).isNotZero(),
                ()-> assertEquals(2, viberBot.getViberPersons().size()),
                ()-> assertThat(mainController.getPersons().size()).isNotZero(),
                ()-> assertEquals(4, mainController.getPersons().size())
        );

        mockMvc.perform(get("/cache/viber/delete-all"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список користувачів, які зараз використовують ботів : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 2;</li>" +
                                "<li>в ВАЙБЕР боті - 0;</li>" +
                                "</ol></center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ТЕЛЕГРАММ БОТ перелік юзерів : </center>" +

                                "<form id='del_telegram_100000000' action='/Complex-Media-Bot/cache/telegram/delete' method='post'>" +
                                "<p><input name='id' form='del_telegram_100000000' value=100000000></p>" +
                                "<p><input type='submit' form='del_telegram_100000000' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : First Telegram;</p>" +
                                "<p>media : first_telegram_media;</p>" +
                                "<p>phone : +38067 111 11 11;</p>" +
                                "<p>email : first_telegram@www.ua;</p>" +
                                "<p>subject : first_telegram_subject;</p>" +
                                "<form id='del_telegram_200000000' action='/Complex-Media-Bot/cache/telegram/delete' method='post'>" +
                                "<p><input name='id' form='del_telegram_200000000' value=200000000></p>" +
                                "<p><input type='submit' form='del_telegram_200000000' value='DELETE'></p>" +
                                "</form>" +
                                "<p>name_surname : Second Telegram;</p>" +
                                "<p>media : second_telegram_media;</p>" +
                                "<p>phone : +38067 222 22 22;</p>" +
                                "<p>email : second_telegram@www.ua;</p>" +
                                "<p>subject : second_telegram_subject;</p>" +

                                "<form id='del_telegram' action='/Complex-Media-Bot/cache/telegram/delete-all' method='get'>" +
                                "<p><input type='submit' form='del_telegram' value='DELETE ALL TELEGRAM'></p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>ВАЙБЕР БОТ перелік юзерів : </center>" ),
                        status().isOk());

        this.mockMvc.perform(post("/cache/viber/delete-all")).andExpect(status().isMethodNotAllowed());
        this.mockMvc.perform(get("/cache/viber/delete-alls")).andExpect(status().isNotFound());

    }

    @Test
    @Order(6)
    void piarsCache () throws Exception{

        mockMvc.perform(get("/cache/all-piars"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список співробітників, які отримують сповіщення від ботів для ЗМІ : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 2;</li>" +
                                "<li>в ВАЙБЕР боті - 2;</li>" +
                                "</ol></center>" +

                                "<form id='telegram_piar_100000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                    "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                    "<br/><input id='piar_id' name='piar_id' form='telegram_piar_100000000' value=100000000></p>" +
                                    "<p>ПІБ співробітника : First Telegram;</p>" +
                                    "<p><input type='submit' form='telegram_piar_100000000' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='telegram_piar_200000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                    "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                    "<br/><input id='piar_id' name='piar_id' form='telegram_piar_200000000' value=200000000></p>" +
                                    "<p>ПІБ співробітника : Second Telegram;</p>" +
                                    "<p><input type='submit' form='telegram_piar_200000000' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Додати співробітника, які отримаують сповіщення від ТЕЛЕГРАМ БОТУ для ЗМІ</center>" +

                                "<form id='add_telegram_piar' action='/Complex-Media-Bot/cache/add-telegram-piar' method='post'>" +
                                    "<p>" +
                                        "<label for='new_telegram_id'>Введіть ID телеграмм співробітника для отримання сповіщення від ТЕЛЕГРАМ боту для ЗМІ : </label>" +
                                    "<br/>" +
                                        "<input id='new_telegram_id' name='new_id' form='add_telegram_piar'>" +
                                    "</p>" +
                                    "<p>" +
                                        "<label for='new_telegram_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                    "<br/>" +
                                        "<input id='new_telegram_name' name='new_name' form='add_telegram_piar'>" +
                                    "</p>" +
                                    "<p>" +
                                        "<input type='submit' form='add_telegram_piar' value='ДОДАТИ'>" +
                                    "</p>" +
                                "</form>" +

                                "<form id='viber_piar_100QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                    "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                    "<br/><input id='piar_id' name='piar_id' form='viber_piar_100QWERTY02' value=100QWERTY02></p>" +
                                    "<p>ПІБ співробітника : First Viber;</p>" +
                                    "<p><input type='submit' form='viber_piar_100QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='viber_piar_200QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                    "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                    "<br/><input id='piar_id' name='piar_id' form='viber_piar_200QWERTY02' value=200QWERTY02></p>" +
                                    "<p>ПІБ співробітника : Second Viber;</p>" +
                                    "<p><input type='submit' form='viber_piar_200QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>Додати співробітника, які отримаують сповіщення від ВАЙБЕР БОТУ для ЗМІ</center>" +
                                "<form id='add_viber_piar' action='/Complex-Media-Bot/cache/add-viber-piar' method='post'>" +
                                    "<p>" +
                                       "<label for='new_viber_id'>Введіть ID телеграмм співробітника для отримання сповіщення від боту для ЗМІ : </label>" +
                                    "<br/>" +
                                        "<input id='new_viber_id' name='new_id' form='add_viber_piar'>" +
                                    "</p>" +
                                    "<p>" +
                                        "<label for='new_viber_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                    "<br/>" +
                                        "<input id='new_viber_name' name='new_name' form='add_viber_piar'></p>" +
                                    "<p>" +
                                        "<input type='submit' form='add_viber_piar' value='ДОДАТИ'>" +
                                    "</p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Видалити всіх співробітників, які отримаують сповіщення від ТЕЛЕГРАМ та ВАЙБЕР БОТІВ для ЗМІ</center>" +
                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-piars' method='get'>" +
                                    "<p><input type='submit' form='del_all_piars' value='ВИДАЛИТИ ВСІХ'></p>" +
                                "</form>"),
                        status().isOk());

        mockMvc.perform(post("/cahce/all-piars")).andExpect(status().isNotFound());
        mockMvc.perform(get("/cahce/all-piar")).andExpect(status().isNotFound());
    }

    @Test
    @Order(1)
    void addTelegramCachePiar () throws Exception {
        mockMvc.perform(post("/cache/add-telegram-piar")
                .param("new_id", "300000000")
                .param("new_name", "Adding Telegram"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список співробітників, які отримують сповіщення від ботів для ЗМІ : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 3;</li>" +
                                "<li>в ВАЙБЕР боті - 2;</li>" +
                                "</ol></center>" +

                                "<form id='telegram_piar_100000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_100000000' value=100000000></p>" +
                                "<p>ПІБ співробітника : First Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_100000000' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='telegram_piar_200000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_200000000' value=200000000></p>" +
                                "<p>ПІБ співробітника : Second Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_200000000' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='telegram_piar_300000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_300000000' value=300000000></p>" +
                                "<p>ПІБ співробітника : Adding Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_300000000' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Додати співробітника, які отримаують сповіщення від ТЕЛЕГРАМ БОТУ для ЗМІ</center>" +

                                "<form id='add_telegram_piar' action='/Complex-Media-Bot/cache/add-telegram-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_telegram_id'>Введіть ID телеграмм співробітника для отримання сповіщення від ТЕЛЕГРАМ боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_id' name='new_id' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_telegram_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_name' name='new_name' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<input type='submit' form='add_telegram_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<form id='viber_piar_100QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_100QWERTY02' value=100QWERTY02></p>" +
                                "<p>ПІБ співробітника : First Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_100QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='viber_piar_200QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_200QWERTY02' value=200QWERTY02></p>" +
                                "<p>ПІБ співробітника : Second Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_200QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>Додати співробітника, які отримаують сповіщення від ВАЙБЕР БОТУ для ЗМІ</center>" +
                                "<form id='add_viber_piar' action='/Complex-Media-Bot/cache/add-viber-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_viber_id'>Введіть ID телеграмм співробітника для отримання сповіщення від боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_viber_id' name='new_id' form='add_viber_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_viber_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_viber_name' name='new_name' form='add_viber_piar'></p>" +
                                "<p>" +
                                "<input type='submit' form='add_viber_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Видалити всіх співробітників, які отримаують сповіщення від ТЕЛЕГРАМ та ВАЙБЕР БОТІВ для ЗМІ</center>" +
                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-piars' method='get'>" +
                                "<p><input type='submit' form='del_all_piars' value='ВИДАЛИТИ ВСІХ'></p>" +
                                "</form>"),
                        status().isOk());

        scanner.deletePiarFromFile(pathTelegram, "300000000", BotLogger.getLogger(LoggerType.TELEGRAM));

        mockMvc.perform(get("/cahce/add-telegram-piar")).andExpect(status().isNotFound());
        mockMvc.perform(post("/cahce/add-telegram-piars")).andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    void deleteTelegramCachePiarsById () throws Exception{
        mockMvc.perform(post("/cache/delete-telegram-piar")
                .param("piar_id", "200000000"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список співробітників, які отримують сповіщення від ботів для ЗМІ : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 1;</li>" +
                                "<li>в ВАЙБЕР боті - 2;</li>" +
                                "</ol></center>" +

                                "<form id='telegram_piar_100000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_100000000' value=100000000></p>" +
                                "<p>ПІБ співробітника : First Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_100000000' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Додати співробітника, які отримаують сповіщення від ТЕЛЕГРАМ БОТУ для ЗМІ</center>" +

                                "<form id='add_telegram_piar' action='/Complex-Media-Bot/cache/add-telegram-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_telegram_id'>Введіть ID телеграмм співробітника для отримання сповіщення від ТЕЛЕГРАМ боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_id' name='new_id' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_telegram_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_name' name='new_name' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<input type='submit' form='add_telegram_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<form id='viber_piar_100QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_100QWERTY02' value=100QWERTY02></p>" +
                                "<p>ПІБ співробітника : First Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_100QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='viber_piar_200QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_200QWERTY02' value=200QWERTY02></p>" +
                                "<p>ПІБ співробітника : Second Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_200QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>Додати співробітника, які отримаують сповіщення від ВАЙБЕР БОТУ для ЗМІ</center>" +
                                "<form id='add_viber_piar' action='/Complex-Media-Bot/cache/add-viber-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_viber_id'>Введіть ID телеграмм співробітника для отримання сповіщення від боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_viber_id' name='new_id' form='add_viber_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_viber_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_viber_name' name='new_name' form='add_viber_piar'></p>" +
                                "<p>" +
                                "<input type='submit' form='add_viber_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Видалити всіх співробітників, які отримаують сповіщення від ТЕЛЕГРАМ та ВАЙБЕР БОТІВ для ЗМІ</center>" +
                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-piars' method='get'>" +
                                "<p><input type='submit' form='del_all_piars' value='ВИДАЛИТИ ВСІХ'></p>" +
                                "</form>"),
                        status().isOk());

        scanner.addPiarToFile(pathTelegram, "200000000:Second Telegram", BotLogger.getLogger(LoggerType.TELEGRAM));

        mockMvc.perform(get("/cahce/all-piars")).andExpect(status().isNotFound());
        mockMvc.perform(get("/cahce/all-piar")).andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void addViberCachePiar () throws Exception {
        mockMvc.perform(post("/cache/add-viber-piar")
                .param("new_id", "300QWERTY02")
                .param("new_name", "Adding Viber"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список співробітників, які отримують сповіщення від ботів для ЗМІ : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 2;</li>" +
                                "<li>в ВАЙБЕР боті - 3;</li>" +
                                "</ol></center>" +

                                "<form id='telegram_piar_100000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_100000000' value=100000000></p>" +
                                "<p>ПІБ співробітника : First Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_100000000' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='telegram_piar_200000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_200000000' value=200000000></p>" +
                                "<p>ПІБ співробітника : Second Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_200000000' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Додати співробітника, які отримаують сповіщення від ТЕЛЕГРАМ БОТУ для ЗМІ</center>" +

                                "<form id='add_telegram_piar' action='/Complex-Media-Bot/cache/add-telegram-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_telegram_id'>Введіть ID телеграмм співробітника для отримання сповіщення від ТЕЛЕГРАМ боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_id' name='new_id' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_telegram_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_name' name='new_name' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<input type='submit' form='add_telegram_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<form id='viber_piar_100QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_100QWERTY02' value=100QWERTY02></p>" +
                                "<p>ПІБ співробітника : First Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_100QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='viber_piar_200QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_200QWERTY02' value=200QWERTY02></p>" +
                                "<p>ПІБ співробітника : Second Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_200QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='viber_piar_300QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_300QWERTY02' value=300QWERTY02></p>" +
                                "<p>ПІБ співробітника : Adding Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_300QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>Додати співробітника, які отримаують сповіщення від ВАЙБЕР БОТУ для ЗМІ</center>" +
                                "<form id='add_viber_piar' action='/Complex-Media-Bot/cache/add-viber-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_viber_id'>Введіть ID телеграмм співробітника для отримання сповіщення від боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_viber_id' name='new_id' form='add_viber_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_viber_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_viber_name' name='new_name' form='add_viber_piar'></p>" +
                                "<p>" +
                                "<input type='submit' form='add_viber_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Видалити всіх співробітників, які отримаують сповіщення від ТЕЛЕГРАМ та ВАЙБЕР БОТІВ для ЗМІ</center>" +
                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-piars' method='get'>" +
                                "<p><input type='submit' form='del_all_piars' value='ВИДАЛИТИ ВСІХ'></p>" +
                                "</form>"),
                        status().isOk());

        scanner.deletePiarFromFile(pathViber, "300QWERTY02", BotLogger.getLogger(LoggerType.VIBER));

        mockMvc.perform(get("/cahce/add-viber-piar")).andExpect(status().isNotFound());
        mockMvc.perform(post("/cahce/add-viber-piars")).andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void deleteViberCachePiarsById () throws Exception {
        mockMvc.perform(post("/cache/delete-viber-piar")
                .param("piar_id", "200QWERTY02"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список співробітників, які отримують сповіщення від ботів для ЗМІ : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 2;</li>" +
                                "<li>в ВАЙБЕР боті - 1;</li>" +
                                "</ol></center>" +

                                "<form id='telegram_piar_100000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_100000000' value=100000000></p>" +
                                "<p>ПІБ співробітника : First Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_100000000' value='ВИДАЛИТИ'></p></form>" +

                                "<form id='telegram_piar_200000000' action='/Complex-Media-Bot/cache/delete-telegram-piar' method='post'>" +
                                "<p><label for=piar_id'>ID співробітника отримуючого сповіщення від боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='telegram_piar_200000000' value=200000000></p>" +
                                "<p>ПІБ співробітника : Second Telegram;</p>" +
                                "<p><input type='submit' form='telegram_piar_200000000' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Додати співробітника, які отримаують сповіщення від ТЕЛЕГРАМ БОТУ для ЗМІ</center>" +

                                "<form id='add_telegram_piar' action='/Complex-Media-Bot/cache/add-telegram-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_telegram_id'>Введіть ID телеграмм співробітника для отримання сповіщення від ТЕЛЕГРАМ боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_id' name='new_id' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_telegram_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_name' name='new_name' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<input type='submit' form='add_telegram_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<form id='viber_piar_100QWERTY02' action='/Complex-Media-Bot/cache/delete-viber-piar' method='post'>" +
                                "<p><label for='piar_id'>ID співробітника отримуючого сповіщення від ВАЙБЕР боту для ЗМІ : </label>" +
                                "<br/><input id='piar_id' name='piar_id' form='viber_piar_100QWERTY02' value=100QWERTY02></p>" +
                                "<p>ПІБ співробітника : First Viber;</p>" +
                                "<p><input type='submit' form='viber_piar_100QWERTY02' value='ВИДАЛИТИ'></p></form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>Додати співробітника, які отримаують сповіщення від ВАЙБЕР БОТУ для ЗМІ</center>" +
                                "<form id='add_viber_piar' action='/Complex-Media-Bot/cache/add-viber-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_viber_id'>Введіть ID телеграмм співробітника для отримання сповіщення від боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_viber_id' name='new_id' form='add_viber_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_viber_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_viber_name' name='new_name' form='add_viber_piar'></p>" +
                                "<p>" +
                                "<input type='submit' form='add_viber_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Видалити всіх співробітників, які отримаують сповіщення від ТЕЛЕГРАМ та ВАЙБЕР БОТІВ для ЗМІ</center>" +
                                "<form id='del_all_bots' action='/Complex-Media-Bot/cache/delete-all-piars' method='get'>" +
                                "<p><input type='submit' form='del_all_piars' value='ВИДАЛИТИ ВСІХ'></p>" +
                                "</form>"),
                        status().isOk());

        scanner.deletePiarFromFile(pathViber, "200QWERTY02:Second Viber", BotLogger.getLogger(LoggerType.VIBER));

        mockMvc.perform(get("/cahce/delete-viber-piar")).andExpect(status().isNotFound());
        mockMvc.perform(post("/cahce/delete-viber-piars")).andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    void deleteAllCachePiars () throws Exception {
        mockMvc.perform(get("/cache/delete-all-piars"))
                .andDo(print())
                .andExpectAll(content().string("<center>Список співробітників, які отримують сповіщення від ботів для ЗМІ : " +
                                "<ol>" +
                                "<li>в ТЕЛЕГРАМ боті - 0;</li>" +
                                "<li>в ВАЙБЕР боті - 0;</li>" +
                                "</ol></center>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +

                                "<center>Додати співробітника, які отримаують сповіщення від ТЕЛЕГРАМ БОТУ для ЗМІ</center>" +

                                "<form id='add_telegram_piar' action='/Complex-Media-Bot/cache/add-telegram-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_telegram_id'>Введіть ID телеграмм співробітника для отримання сповіщення від ТЕЛЕГРАМ боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_id' name='new_id' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_telegram_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_telegram_name' name='new_name' form='add_telegram_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<input type='submit' form='add_telegram_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>" +

                                "<hr style='width:50%;text-align:left;margin-left:0'>" +
                                "<center>Додати співробітника, які отримаують сповіщення від ВАЙБЕР БОТУ для ЗМІ</center>" +
                                "<form id='add_viber_piar' action='/Complex-Media-Bot/cache/add-viber-piar' method='post'>" +
                                "<p>" +
                                "<label for='new_viber_id'>Введіть ID телеграмм співробітника для отримання сповіщення від боту для ЗМІ : </label>" +
                                "<br/>" +
                                "<input id='new_viber_id' name='new_id' form='add_viber_piar'>" +
                                "</p>" +
                                "<p>" +
                                "<label for='new_viber_name'>Введіть ім'я та прізвище співробітника : </label>" +
                                "<br/>" +
                                "<input id='new_viber_name' name='new_name' form='add_viber_piar'></p>" +
                                "<p>" +
                                "<input type='submit' form='add_viber_piar' value='ДОДАТИ'>" +
                                "</p>" +
                                "</form>"),
                        status().isOk());


        scanner.addPiarToFile(pathTelegram, "100000000:First Telegram", BotLogger.getLogger(LoggerType.TELEGRAM));
        scanner.addPiarToFile(pathTelegram, "200000000:Second Telegram", BotLogger.getLogger(LoggerType.TELEGRAM));

        scanner.addPiarToFile(pathViber, "100QWERTY02:First Viber", BotLogger.getLogger(LoggerType.VIBER));
        scanner.addPiarToFile(pathViber, "200QWERTY02:Second Viber", BotLogger.getLogger(LoggerType.VIBER));


        mockMvc.perform(post("/cahce/delete-all-piars")).andExpect(status().isNotFound());
        mockMvc.perform(get("/cahce/delete-all-piar")).andExpect(status().isNotFound());
    }
}