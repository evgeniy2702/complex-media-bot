package ua.ukrposhta.complexmediabot.model.xml_files_entities.textButtonsOfBot;

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
 * This class describe list of button entity for saving data from different xml file.
 */


@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"buttonEntities"}, callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "buttons" )
@XmlAccessorType(XmlAccessType.FIELD)
public class ButtonEntityList extends AbstractEntityList<ButtonEntity> {

    @XmlElement(name = "button")
    private List<ButtonEntity> buttonEntities;

}
