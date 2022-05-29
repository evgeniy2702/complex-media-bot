package ua.ukrposhta.complexmediabot.model.user;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public abstract class PersonEntity {

    String currentStateName;
    String prevStateName;
    String mediaName;
    String name_surname;
    String phone;
    String email;
    String subject;
    String messagePath;
    String buttonPath;
    String errorPath;
    LocalDateTime addDate;
    LocalDateTime dateUnsubscribe;
    boolean activity;
    boolean exit;
}
