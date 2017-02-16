package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class IdUtils {

    private static final String ID_DELIMITER = ":";

    public static String instanceIdToString(InstanceId instanceId) {
        ZoneId zoneId = instanceId.getZoneId();
        return join(zoneId.getProject(), zoneId.getZone(), instanceId.getInstance());
    }

    public static InstanceId stringToInstanceId(String instanceId) {
        String[] pieces = split(instanceId, 3);
        return InstanceId.of(pieces[0], pieces[1], pieces[2]);
    }

    public static String networkIdToString(NetworkId networkId) {
        return join(networkId.getProject(), networkId.getNetwork());
    }

    public static NetworkId stringToNetworkId(String networkId) {
        String[] pieces = split(networkId, 2);
        return NetworkId.of(pieces[0], pieces[1]);
    }

    public static String subnetworkIdToString(SubnetworkId subnetworkId) {
        return join(subnetworkId.getProject(), subnetworkId.getRegion(), subnetworkId.getSubnetwork());
    }

    public static SubnetworkId stringToSubnetworkId(String subnetworkId) {
        String[] pieces = split(subnetworkId, 3);
        return SubnetworkId.of(pieces[0], pieces[1], pieces[3]);
    }

    public static String machineTypeIdToString(MachineTypeId machineTypeId) {
        ZoneId zoneId = machineTypeId.getZoneId();
        return join(zoneId.getProject(), zoneId.getZone(), machineTypeId.getType());
    }

    public static MachineTypeId stringToMachineTypeId(String flavorId) {
        String[] pieces = split(flavorId, 3);
        return MachineTypeId.of(pieces[0], pieces[1], pieces[2]);
    }

    public static String imageIdToString(ImageId imageId) {
        return join(imageId.getProject(), imageId.getImage());
    }

    public static ImageId stringToImageId(String imageId) {
        String[] pieces = split(imageId, 2);
        return ImageId.of(pieces[0], pieces[1]);
    }

    private static String[] split(String input, int piecesRequired) {
        String[] pieces = String.valueOf(input).split(ID_DELIMITER);
        if (pieces.length != piecesRequired) {
            throw new IllegalArgumentException(String.format(
                    "Invalid ID [%s] - should be %d colon delimited values",
                    input,
                    piecesRequired
            ));
        }
        return pieces;
    }

    private static String join(String... pieces) {
        return Arrays.stream(pieces).collect(Collectors.joining(ID_DELIMITER));
    }

}
