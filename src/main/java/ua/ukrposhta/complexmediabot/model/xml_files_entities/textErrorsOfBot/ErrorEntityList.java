package ua.ukrposhta.complexmediabot.model.xml_files_entities.textErrorsOfBot;

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
 * This class describe list of errors entity for saving data from different xml file.
 */

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"errorEntities"}, callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "errors" )
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorEntityList extends AbstractEntityList<ErrorEntity> {

    @XmlElement(name = "error")
    private List<ErrorEntity> errorEntities;
}
