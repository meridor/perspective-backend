package org.meridor.perspective.rest.data.converters;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.beans.*;

import java.util.stream.Stream;

public final class ProjectConverters {
    
    public static Stream<Cloud> projectToCloud(Project p) {
        return Stream.of(new Cloud(p.getCloudId(), p.getCloudType().name().toLowerCase()));
    }
    
    public static Stream<ProjectMetadata> projectToMetadata(Project p) {
        return p.getMetadata().keySet().stream()
                .map(k -> new ProjectMetadata(p.getId(), k.toString().toLowerCase(), p.getMetadata().get(k)));
    }
    
    public static Stream<ExtendedAvailabilityZone> projectToAvailabilityZones(Project p) {
        return p.getAvailabilityZones().stream().map(k -> new ExtendedAvailabilityZone(p.getId(), k));
    }
    
    public static Stream<ExtendedFlavor> projectToFlavors(Project p) {
        return p.getFlavors().stream().map(f -> new ExtendedFlavor(p.getId(), f));
    }
    
    public static Stream<ExtendedKeypair> projectToKeypairs(Project p) {
        return p.getKeypairs().stream().map(k -> new ExtendedKeypair(p.getId(), k));
    }
    
    public static Stream<ExtendedNetwork> projectToNetworks(Project p) {
        return p.getNetworks().stream().map(n -> new ExtendedNetwork(p.getId(), n));
    }
    
    public static Stream<ExtendedNetworkSubnet> projectToNetworkSubnets(Project p) {
        return p.getNetworks().stream().flatMap(n ->
                n.getSubnets().stream()
                        .map(s -> new ExtendedNetworkSubnet(p.getId(), n.getId(), s))
        );
    }
    
    private ProjectConverters() {
        
    }
    
}
