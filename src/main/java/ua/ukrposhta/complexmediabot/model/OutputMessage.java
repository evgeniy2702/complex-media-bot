package ua.ukrposhta.complexmediabot.model;


import lombok.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ua.ukrposhta.complexmediabot.bot.BotContext;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonKeyboard;
import ua.ukrposhta.complexmediabot.viberBot.keyboard.ViberKeyboard;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe output massage for send it telegram server .
 */

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"chat_id", "message_text", "replyKeyboardReply", "inlineKeyboardMarkup"})
@EqualsAndHashCode(of = {"message_text"})
public class OutputMessage {

    private String chat_id;
    private String message_text;
    private BotContext context;
    private CommonKeyboard replyKeyboardReply;
    private InlineKeyboardMarkup inlineKeyboardMarkup;
    private ViberKeyboard viberKeyboard;
}
