package ua.ukrposhta.complexmediabot.model.xml_files_entities.textMessageOfBot;

import lombok.*;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.AbstractEntityList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe list of message entity for saving data from different xml file.
 */

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"messageEntities"}, callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "messages" )
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageEntityList extends AbstractEntityList<MessageEntity> {

    @XmlElement(name = "message")
    private List<MessageEntity> messageEntities;

}
