package ua.ukrposhta.complexmediabot.bot;

import ua.ukrposhta.complexmediabot.utils.type.BotType;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * Interface for identify getTypeSender method in classes , which will be
 * to implement it
 */

interface TypedSender extends Sender {

    BotType getBotType();
}

