package ua.ukrposhta.complexmediabot.utils.type;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This is enum of type of viber message, which can use this project.
 */

public enum ViberTypeMessage {

    SEND_MESSAGE("send_message"),
    BROADCAST_MESSAGE("broadcast_message"),
    GET_ACCOUNT_INFO("get_account_info"),
    GET_USER_DETAILS("get_user_details"),
    GET_ONLINE("get_online")
    ;

    private String text;

    ViberTypeMessage(String text){
        this.text = text;
    }

    public String getText(){
        return this.text;
    }
}
