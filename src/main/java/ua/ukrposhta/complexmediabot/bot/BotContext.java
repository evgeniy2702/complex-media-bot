package ua.ukrposhta.complexmediabot.bot;

import lombok.Getter;
import lombok.Setter;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.TelegramPersonEntity;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.viberBot.entityUser.ViberPersonEntity;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe the current context of user and bot. It consist from
 * - type of bot
 * - telegram bot
 * - user
 * - string value of input
 */

@Getter
@Setter
public class BotContext  {

    private BotType typeBot;
    private TelegramBot telegramBot;
    private BotViber viberBot;
    private TelegramPersonEntity telegramPerson;
    private ViberPersonEntity viberPerson;
    private String input;

    private BotContext(BotType typeBot,
                      TelegramBot bot,
                      TelegramPersonEntity person,
                      String input) {
        this.typeBot = typeBot;
        this.telegramBot = bot;
        this.telegramPerson = person;
        this.input = input;
    }

    private BotContext(BotType typeBot,
                       BotViber bot,
                      ViberPersonEntity person,
                      String input) {
        this.typeBot = typeBot;
        this.viberBot = bot;
        this.viberPerson = person;
        this.input = input;
    }

    public static BotContext ofTelegram(BotType typeBot,
                                         TelegramBot bot,
                                         TelegramPersonEntity person,
                                         String input){
        return new BotContext(typeBot, bot, person, input);
    }

    public static BotContext ofViber(BotType typeBot,
                                     BotViber bot,
                                      ViberPersonEntity person,
                                      String input){
        return new BotContext(typeBot, bot, person, input);
    }
}
