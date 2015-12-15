package org.meridor.perspective.beans;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

@XmlJavaTypeAdapter(MetadataMapAdapter.class)
public class MetadataMap extends HashMap<MetadataKey, String> {
}
