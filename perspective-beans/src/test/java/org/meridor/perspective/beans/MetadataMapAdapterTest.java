package org.meridor.perspective.beans;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MetadataMapAdapterTest {
    
    @Test
    public void testMarshalAndUnmarshal() {
        MetadataMapAdapter adapter = new MetadataMapAdapter();
        MetadataMap metadataMap = new MetadataMap(){
            {
                put(MetadataKey.ID, "test-id");
            }
        };
        Metadata metadata = adapter.marshal(metadataMap);
        assertThat(adapter.unmarshal(metadata), equalTo(metadataMap));
    }

}