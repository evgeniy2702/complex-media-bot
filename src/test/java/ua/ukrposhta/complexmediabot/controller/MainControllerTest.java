package ua.ukrposhta.complexmediabot.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.ukrposhta.complexmediabot.telegramBot.messageHandler.TelegramIncomingMessageHandler;
import ua.ukrposhta.complexmediabot.utils.TxtFileScanner;
import ua.ukrposhta.complexmediabot.viberBot.messageHandler.ViberIncomingMessageHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        MainController.class
})
@TestPropertySource(locations = {
        "classpath:application-dev.properties"
})
@ActiveProfiles("dev")
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${viberbot.token}")
    private String token;

    @MockBean
    private TelegramIncomingMessageHandler telegramIncomingMessageHandler;
    @MockBean
    private ViberIncomingMessageHandler viberIncomingMessageHandler;
    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private TxtFileScanner scanner;

    private MainController mainController;

    @BeforeEach
    void setUp(){
        mainController = new MainController(telegramIncomingMessageHandler,
                viberIncomingMessageHandler,
                objectMapper, scanner);
    }


    @Test
    void receiveTelegramUpdate () throws Exception {
        assertAll(
                ()-> assertThat(mainController).isNotNull(),
                ()-> assertThat(restTemplate).isNotNull()
        );

        this.mockMvc.perform(post("/telegram")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new Update())))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void receiveViberUpdate () throws Exception {
        assertAll(
                ()-> assertThat(mainController).isNotNull(),
                ()-> assertThat(restTemplate).isNotNull()
        );

        this.mockMvc.perform(post("/viber")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new JSONObject()))
                    .header("X-Viber-Content-Signature",
                            token))
                .andDo(print())
                .andExpect(status().isOk());
    }
}