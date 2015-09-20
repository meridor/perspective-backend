package org.meridor.perspective.beans;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

public class MapAdapter extends XmlAdapter<Metadata, MetadataMap> {
    @Override
    public MetadataMap unmarshal(Metadata metadata) {
        MetadataMap map = new MetadataMap();
        for (Metadata.Entry e : metadata.getEntries()) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

    @Override
    public Metadata marshal(MetadataMap map) {
        Metadata metadata = new Metadata();
        for (Map.Entry<MetadataKey, String> entry : map.entrySet()){
            Metadata.Entry e = new Metadata.Entry();
            e.setKey(entry.getKey());
            e.setValue(entry.getValue());
            metadata.getEntries().add(e);
        }
        return metadata;
    }
}
