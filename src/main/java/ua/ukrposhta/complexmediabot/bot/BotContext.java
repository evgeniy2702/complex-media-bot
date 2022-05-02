package ua.ukrposhta.complexmediabot.bot;

import lombok.Getter;
import lombok.Setter;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.Person;
import ua.ukrposhta.complexmediabot.utils.type.BotType;

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
public class BotContext {

    private BotType typeBot;
    private TelegramBot bot;
    private Person person;
    private String input;

    public static BotContext of(BotType typeBot,
                                TelegramBot bot,
                                Person person,
                                String input){
        return new BotContext(typeBot, bot, person, input);
    }

    private BotContext(BotType typeBot,
                      TelegramBot bot,
                      Person person,
                      String input) {
        this.typeBot = typeBot;
        this.bot = bot;
        this.person = person;
        this.input = input;
    }
}
