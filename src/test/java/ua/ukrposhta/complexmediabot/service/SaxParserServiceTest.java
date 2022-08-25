package ua.ukrposhta.complexmediabot.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.parserXml.MySaxParserForButtons;
import ua.ukrposhta.complexmediabot.utils.parserXml.MySaxParserForErrors;
import ua.ukrposhta.complexmediabot.utils.parserXml.MySaxParserForMessage;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;
import ua.ukrposhta.complexmediabot.utils.type.ParserHandlerType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SaxParserServiceTest {

    @Autowired
    private SaxParserService service;


    @Test
    void getParser () {

        MySaxParserForMessage parserForMessage = (MySaxParserForMessage) service.getParser(ParserHandlerType.MESSAGE);
        MySaxParserForButtons parserForButtons = (MySaxParserForButtons) service.getParser(ParserHandlerType.BUTTON);
        MySaxParserForErrors parserForErrors = (MySaxParserForErrors) service.getParser(ParserHandlerType.ERROR);

        assertAll(
                ()-> assertThat(parserForMessage).isNotNull(),
                ()-> assertThat(parserForButtons).isNotNull(),
                ()->assertThat(parserForErrors).isNotNull(),
                ()->assertEquals(parserForMessage.getConsoleLogger(), BotLogger.getLogger(LoggerType.CONSOLE)),
                ()->assertEquals(parserForMessage.getTelegramLogger(), BotLogger.getLogger(LoggerType.TELEGRAM)),
                ()->assertEquals(parserForMessage.getViberLogger(), BotLogger.getLogger(LoggerType.VIBER))
        );
    }
}