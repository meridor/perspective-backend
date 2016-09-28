package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.sql.QueryResult;

import java.util.Arrays;
import java.util.Collections;

import static org.meridor.perspective.beans.MetadataKey.REGION;
import static org.meridor.perspective.shell.common.repository.impl.QueryUtils.createQueryResult;

public final class MockQueryResults {
    static QueryResult createMockInstancesResult(
            Project project,
            Instance instance,
            Image image,
            Flavor flavor
    ) {
        return createQueryResult(
                Arrays.asList(
                        "instances.id",
                        "instances.real_id",
                        "instances.name",
                        "projects.id",
                        "projects.name",
                        "instances.cloud_id",
                        "instances.cloud_type",
                        "images.name",
                        "flavors.name",
                        "instances.addresses",
                        "instances.state",
                        "instances.last_updated"
                ),
                Collections.singletonList(Arrays.asList(
                        instance.getId(),
                        instance.getRealId(),
                        instance.getName(),
                        project.getId(),
                        project.getName(),
                        instance.getCloudId(),
                        instance.getCloudType().value(),
                        image.getName(),
                        flavor.getName(),
                        instance.getAddresses().get(0),
                        instance.getState().value(),
                        instance.getTimestamp()
                ))
        );

    }

    static QueryResult createMockInstanceMetadataResult(Instance instance) {
        return createQueryResult(
                Arrays.asList(
                        "instance_id",
                        "key",
                        "value"
                ),
                Collections.singletonList(Arrays.asList(
                        instance.getId(),
                        REGION.value(),
                        instance.getMetadata().get(REGION)
                ))
        );
    }

    static QueryResult createMockImagesResult(Project project, Image image) {
        return createQueryResult(
                Arrays.asList(
                        "images.id",
                        "images.real_id",
                        "images.name",
                        "images.cloud_type",
                        "images.state",
                        "images.last_updated",
                        "projects.id",
                        "projects.name"
                ),
                Collections.singletonList(Arrays.asList(
                        image.getId(),
                        image.getRealId(),
                        image.getName(),
                        image.getCloudType().value(),
                        image.getState().value(),
                        image.getTimestamp(),
                        project.getId(),
                        project.getName()
                ))
        );
    }

    static QueryResult createMockProjectsResult(Project project) {
        return createQueryResult(
                Arrays.asList(
                        "id", "name", "cloud_id", "cloud_type",
                        "instances", "vcpus", "ram", "disk", "ips",
                        "security_groups", "volumes", "keypairs"
                ),
                Collections.singletonList(Arrays.asList(
                        project.getId(),
                        project.getName(),
                        project.getCloudId(),
                        project.getCloudType().value(),
                        project.getQuota().getInstances(),
                        null, null, null, null, null, null, null
                ))
        );
    }

    static QueryResult createMockNetworksResult(Project project, Network network) {
        return createQueryResult(
                Arrays.asList("networks.id", "networks.name", "projects.name", "networks.state", "networks.is_shared", "network_subnets.cidr"),
                Collections.singletonList(Arrays.asList(
                        network.getId(),
                        network.getName(),
                        project.getName(),
                        network.getState(),
                        String.valueOf(network.isIsShared()),
                        "5.255.255.0/24"
                ))
        );
    }

    static QueryResult createMockFlavorsResult(Project project, Flavor flavor) {
        return createQueryResult(
                Arrays.asList("flavors.id", "flavors.name", "projects.name", "flavors.vcpus", "flavors.ram", "flavors.root_disk", "flavors.ephemeral_disk"),
                Collections.singletonList(Arrays.asList(
                        flavor.getId(),
                        flavor.getName(),
                        project.getName(),
                        flavor.getVcpus(),
                        flavor.getRam(),
                        flavor.getRootDisk(),
                        flavor.getEphemeralDisk()
                ))
        );
    }

    static QueryResult createMockKeypairsResult(Project project, Keypair keypair) {
        return createQueryResult(
                Arrays.asList("keypairs.name", "keypairs.fingerprint", "projects.name"),
                Collections.singletonList(Arrays.asList(
                        keypair.getName(),
                        keypair.getFingerprint(),
                        project.getName()
                ))
        );
    }
}
