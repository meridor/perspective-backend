package org.meridor.perspective.beans;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

@XmlJavaTypeAdapter(MapAdapter.class)
public class MetadataMap extends HashMap<MetadataKey, String> {
}
