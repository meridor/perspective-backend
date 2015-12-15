package org.meridor.perspective.sql;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

@XmlJavaTypeAdapter(DataRowMapAdapter.class)
public class DataRowMap extends HashMap<String, Object> {
}
