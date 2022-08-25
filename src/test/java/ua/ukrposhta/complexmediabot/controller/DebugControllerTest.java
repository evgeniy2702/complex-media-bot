package ua.ukrposhta.complexmediabot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@WebMvcTest(controllers = {
        DebugController.class
})
class DebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void getDebugMessageGet () throws Exception {

        assertThat(restTemplate).isNotNull();

         this.mockMvc.perform(get("/debug"))
                 .andDo(print())
                 .andExpect(content()
                         .string(containsString("<center><br>HELLO DEBUG! COMPLEX_MEDIA_BOT is WORKING!</br></center>")));
    }
}