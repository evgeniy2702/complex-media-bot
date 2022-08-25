package ua.ukrposhta.complexmediabot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("dev")
class ComplexMediaBotApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void main () {
        ComplexMediaBotApplication.main(new String[]{});
    }

    @Test
    @DisplayName("Testing context application and print all beans")
    void contextLoad(){
        assertThat(context).isNotNull();

        System.out.println("ContextLoad");

        String[] beans = context.getBeanDefinitionNames();

        for(String bean : beans){
            System.out.println("***" + bean);
        }
    }
}