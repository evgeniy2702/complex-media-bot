package ua.ukrposhta.complexmediabot.telegramBot.message;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe incoming massage from telegram server to our controller for telegram bot.
 */

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(of={"chat_id"})
@ToString
public class IncomingMessage {

    private String text;

    // Data about telegram User
    private Long chat_id;
    private Boolean isBot;
    private String firstName;
    private String lastName;
    private String userName;
    private String languageCode;
}
