package ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot;

import lombok.*;
import ua.ukrposhta.complexmediabot.model.xml_files_entities.AbstractEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe entity for saving data from errors.xml file.
 */

@Getter
@Setter
@EqualsAndHashCode(of={"id"},callSuper = true)
@ToString(of = "id", callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorEntity extends AbstractEntity {

    @XmlElement(name = "id")
    private String id;
}
