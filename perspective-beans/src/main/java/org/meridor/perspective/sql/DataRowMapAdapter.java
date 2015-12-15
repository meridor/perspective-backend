package org.meridor.perspective.sql;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

public class DataRowMapAdapter extends XmlAdapter<DataRow, DataRowMap> {
    
    @Override
    public DataRowMap unmarshal(DataRow metadata) {
        DataRowMap map = new DataRowMap();
        for (DataRow.Entry e : metadata.getEntries()) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

    @Override
    public DataRow marshal(DataRowMap map) {
        DataRow dataRow = new DataRow();
        for (Map.Entry<String, Object> entry : map.entrySet()){
            DataRow.Entry e = new DataRow.Entry();
            e.setKey(entry.getKey());
            e.setValue(entry.getValue());
            dataRow.getEntries().add(e);
        }
        return dataRow;
    }
}
