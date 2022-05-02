package ua.ukrposhta.complexmediabot.bot;

import lombok.Getter;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This enum for sets and describes the state of bot in work process,
 * which will be sets to users according algorithm of work of bot
 */
@Getter
public enum  BotState {

    START("/start"),
    LANGUAGE("LANGUAGE"),
    GOOD_DAY("GOOD_DAY"),
    SELECT("SELECT"),
    MEDIA("MEDIA"),
    NAME_SURNAME("NAME_SURNAME"),
    PHONE("PHONE"),
    EMAIL("EMAIL"),
    SUBJECT("SUBJECT"),
    WE_CONTACT("WE_CONTACT"),
    END("END"),
    ERROR("ERROR"),
    PIAR_UNIT("PIAR_UNIT")
    ;

    private static BotState[] states;
    public String name;

    BotState(String  name){
        this.name = name;
    }

    public static  BotState getInitialState(){
        return byId(0);
    }

    public static BotState byId(int id) {

        if(states == null){
            states = BotState.values();
        }
        return states[id];
    }
}
