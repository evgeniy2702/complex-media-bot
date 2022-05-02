package ua.ukrposhta.complexmediabot.model.xml_files_entities;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Zhurenko Evgeniy
 *
 * @apiNote
 * This class describe abstract entity for saving data from different xml file.
 */

@Getter
@Setter
public abstract class AbstractEntity {

    @XmlElement(name = "type")
    String type;
    @XmlElement(name = "txt")
    String txt;

}
