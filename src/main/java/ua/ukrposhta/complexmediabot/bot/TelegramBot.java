package ua.ukrposhta.complexmediabot.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonButton;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonKeyboard;
import ua.ukrposhta.complexmediabot.model.keyboard.CommonRow;
import ua.ukrposhta.complexmediabot.telegramBot.message.OutputMessage;
import ua.ukrposhta.complexmediabot.telegramBot.entityUser.Person;
import ua.ukrposhta.complexmediabot.utils.exception.SenderException;
import ua.ukrposhta.complexmediabot.utils.logger.BotLogger;
import ua.ukrposhta.complexmediabot.utils.logger.ConsoleLogger;
import ua.ukrposhta.complexmediabot.utils.logger.TelegramLogger;
import ua.ukrposhta.complexmediabot.utils.type.BotType;
import ua.ukrposhta.complexmediabot.utils.type.LoggerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is main class, which extends from TelegramWebhookBot and implement TypeSender interface.
 * It sets the logic of send message in to Telegram server
 */


@Setter
public class TelegramBot extends TelegramWebhookBot implements TypedSender {

    private String botUserName;
    private String botPath;
    private String botToken;

    public static final int MAX_ROWS_COUNT = 2;
    private Map<Long, Person> persons = new HashMap<>();
    private BotLogger consoleLogger = ConsoleLogger.getLogger(LoggerType.CONSOLE);
    private BotLogger telegramLogger = TelegramLogger.getLogger(LoggerType.TELEGRAM);

    public TelegramBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

//    Формирует результирующее сообщение для отправки на сервер Телеграмма
    public void send(OutputMessage message) throws SenderException {
        consoleLogger.info("START send method in TelegramBot.class");
        telegramLogger.info("TelegramBot.class  OutputMessage : " + message.toString());
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getContext().getPerson().getIncomingTelegramMessage().getChat_id()));
            sendMessage.setText(replaceMarkdown(message.getMessage_text()));
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(replyKeyboard(message.getReplyKeyboardReply()));
            try {
                telegramLogger.info("TelegramBot.class  SendMessage - " + new ObjectMapper().writeValueAsString(sendMessage));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new SenderException(e.getMessage(), message.getContext().getTypeBot());
            }
            execute(sendMessage);
        } catch (TelegramApiException e) {
            handlerException(e, message);
            consoleLogger.info("ERROR : " + e.getMessage());
            consoleLogger.info("CAUSE : " + e.getCause());
            telegramLogger.info("ERROR : " + e.getMessage());
            telegramLogger.info("CAUSE : " + e.getCause());
            throw new SenderException(BotType.TELEGRAM, e.getCause());
        }
    }

//    Формирует результирующее сообщение с InlineKeyboardMarkup для оптравки на сервер Телеграмма
    public void sendInlineKeyboard(OutputMessage message) throws SenderException{

        consoleLogger.info("START sendInlineKeyboard method in TelegramBot.class");
        telegramLogger.info("START sendInlineKeyboard method in TelegramBot.class");
        telegramLogger.info("OutputMessage in sendInlineKeyboard : " + message.toString());
        try {
            SendMessage sendMessageWithInline = new SendMessage();
            sendMessageWithInline.setChatId(String.valueOf(message.getContext().getPerson()
                    .getIncomingTelegramMessage().getChat_id()));
            sendMessageWithInline.setText(message.getMessage_text());
            sendMessageWithInline.setReplyMarkup(message.getInlineKeyboardMarkup());
            try {
                telegramLogger.info("SendMessage with inline keyboard- " + new ObjectMapper()
                        .writeValueAsString(sendMessageWithInline));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new SenderException(e.getMessage(), message.getContext().getTypeBot());
            }
            execute(sendMessageWithInline);
        } catch (TelegramApiException e) {
            handlerException(e, message);
            throw new SenderException(BotType.TELEGRAM, e.getCause());
        }
    }

//     Формирует ReplyKeyboardMarkup для резуьтирующего сообщения на сервер Телеграмм
    private ReplyKeyboard replyKeyboard(CommonKeyboard commonKeyboard) {
        if (commonKeyboard == null || commonKeyboard.size() == 0) {
            return new ReplyKeyboardRemove();
        }
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        replyKeyboard.setOneTimeKeyboard(true);


        List<KeyboardRow> keyboardRows = new ArrayList<>();
        replyKeyboard.setKeyboard(keyboardRows);
        int counter = 0;
        while (true) {
            List<CommonRow> rowList = commonKeyboard.getRowList();
            for (CommonRow commonButtons : rowList) {
                KeyboardRow keyboardRow = new KeyboardRow();
                keyboardRows.add(keyboardRow);
                if (counter < MAX_ROWS_COUNT && counter < commonKeyboard.size()) {
                    for (CommonButton button : commonButtons) {
                        keyboardRow.add(new KeyboardButton(button.getText()));
                    }
                } else {
                    return replyKeyboard;
                }
                counter++;
            }
        }
    }


    @Override
    public BotType getBotType() {
        return BotType.TELEGRAM;
    }

    private String replaceMarkdown(String text) {
        return text.replace("_", "\\_")
                .replace("`", "\\`")
                .replace("*", "\\*");
    }

//     Обрабатывает exception в случае получения ошибок 403 и 400
    private void handlerException(Exception e, OutputMessage message){
        if (e instanceof TelegramApiRequestException) {
            TelegramApiRequestException requestException = (TelegramApiRequestException) e;
            Integer errorCode = requestException.getErrorCode();
            String errorString = requestException.getMessage();
            if (errorCode == HttpStatus.FORBIDDEN.value() &&
                    errorString.equals("Forbidden: bot was blocked by the user")) {
                consoleLogger.info("ERROR in handlerException : " + HttpStatus.FORBIDDEN.getReasonPhrase() + " " +
                        HttpStatus.FORBIDDEN.value());
                telegramLogger.info("ERROR in handlerException : " + HttpStatus.FORBIDDEN.getReasonPhrase() + " " +
                        HttpStatus.FORBIDDEN.value());
                persons.remove(message.getContext().getPerson().getIncomingTelegramMessage().getChat_id());
            } else if(errorCode == HttpStatus.BAD_REQUEST.value() &&
                    errorString.equals("Bad Request: chat not found")){
                consoleLogger.info("ERROR in handlerException : " + HttpStatus.BAD_REQUEST.getReasonPhrase() + " " +
                        HttpStatus.BAD_REQUEST.value());
                telegramLogger.info("ERROR in handlerException : " + HttpStatus.BAD_REQUEST.getReasonPhrase() + " " +
                        HttpStatus.BAD_REQUEST.value());
                persons.remove(message.getContext().getPerson().getIncomingTelegramMessage().getChat_id());
            }
        }
    }
}

