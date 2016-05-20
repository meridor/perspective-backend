package org.meridor.perspective.rest.data.converters;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.data.beans.InstanceMetadata;
import org.meridor.perspective.rest.data.beans.InstanceNetwork;

import java.util.stream.Stream;

public final class InstanceConverters {
    
    public static Stream<InstanceMetadata> instanceToMetadata(Instance i) {
        return i.getMetadata().keySet().stream()
                .map(k -> new InstanceMetadata(i.getId(), k.toString().toLowerCase(), i.getMetadata().get(k)));
    }
    
    public static Stream<InstanceNetwork> instanceToNetworks(Instance i) {
        return i.getNetworks().stream()
                .map(n -> new InstanceNetwork(i.getId(), n.getId()));
    }
    
    private InstanceConverters() {
        
    }
}
