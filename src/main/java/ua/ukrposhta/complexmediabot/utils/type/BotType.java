package ua.ukrposhta.complexmediabot.utils.type;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is enum of type of bots, which can use this project.
 */

public enum BotType {
    TELEGRAM("telegram"),
    VIBER("viber");

    private String text;

    BotType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public BotType getBotType(String type) {
        switch (type) {
            case "telegram":
                return TELEGRAM;
            case "viber":
                return VIBER;
            default:
                return null;
        }
    }
}
