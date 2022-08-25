package ua.ukrposhta.complexmediabot.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForButtons;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForErrors;
import ua.ukrposhta.complexmediabot.utils.handlerXml.MySaxHandlerForMessage;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class SaxHandlerServiceTest {

    @Autowired
    private SaxHandlerService service;

    @Test
    void getHandler () {

        MySaxHandlerForMessage handlerForMessage = (MySaxHandlerForMessage) service.getHandler(ParserHandlerType.MESSAGE);
        MySaxHandlerForButtons handlerForButtons = (MySaxHandlerForButtons) service.getHandler(ParserHandlerType.BUTTON);
        MySaxHandlerForErrors handlerForErrors = (MySaxHandlerForErrors) service.getHandler(ParserHandlerType.ERROR);

        assertAll(
                ()-> assertThat(handlerForMessage).isNotNull(),
                ()-> assertThat(handlerForButtons).isNotNull(),
                ()->assertThat(handlerForErrors).isNotNull(),
                ()->assertEquals(BotLogger.getLogger(LoggerType.CONSOLE), handlerForMessage.getConsoleLogger()),
                ()->assertEquals(BotLogger.getLogger(LoggerType.TELEGRAM),
                        handlerForButtons.getLogger(LoggerType.TELEGRAM.name())),
                ()->assertEquals(BotLogger.getLogger(LoggerType.VIBER),
                        handlerForErrors.getLogger(LoggerType.VIBER.name()))
        );
    }
}