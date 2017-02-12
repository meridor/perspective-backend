package org.meridor.perspective.aws;

import com.amazonaws.services.ec2.model.InstanceType;
import org.meridor.perspective.beans.Flavor;

public enum AvailableFlavor {
    C1medium(InstanceType.C1Medium, 2, 1.7, 350, "1medium 2 1.7 350 1 x 350 HDD; deprecated"),
    C1xlarge(InstanceType.C1Xlarge, 8, 7, 1680, "4 x 420 HDD; deprecated"),
    C32xlarge(InstanceType.C32xlarge, 8, 15, 160, "Intel Xeon E5-2680 v2 2.8 GHz; 2 x 80 SSD"),
    C34xlarge(InstanceType.C34xlarge, 16, 30, 320, "Intel Xeon E5-2680 v2 2.8 GHz; 2 x 160 SSD"),
    C38xlarge(InstanceType.C38xlarge, 32, 60, 640, "Intel Xeon E5-2680 v2 2.8 GHz; 2 x 320 SSD"),
    C3large(InstanceType.C3Large, 2, 3.75, 32, "Intel Xeon E5-2680 v2 2.8 GHz; 2 x 16 SSD"),
    C3xlarge(InstanceType.C3Xlarge, 4, 7.5, 80, "Intel Xeon E5-2680 v2 2.8 GHz; 2 x 40 SSD"),
    C42xlarge(InstanceType.C42xlarge, 8, 15, 0, "Intel Xeon E5-2666 v3 2.9 GHz"),
    C44xlarge(InstanceType.C44xlarge, 16, 30, 0, "Intel Xeon E5-2666 v3 2.9 GHz"),
    C48xlarge(InstanceType.C48xlarge, 36, 60, 0, "Intel Xeon E5-2666 v3 2.9 GHz"),
    C4large(InstanceType.C4Large, 2, 3.75, 0, "Intel Xeon E5-2666 v3 2.9 GHz"),
    C4xlarge(InstanceType.C4Xlarge, 4, 7.5, 0, "Intel Xeon E5-2666 v3 2.9 GHz"),
    Cc28xlarge(InstanceType.Cc28xlarge, 32, 60.5, 3360, "4 x 840 HDD; deprecated"),
    cg14xlarge(InstanceType.Cg14xlarge, 16, 22.5, 1680, "22.5 1680 2 x 840 HDD; deprecated"),
    Cr18xlarge(InstanceType.Cr18xlarge, 32, 244, 240, "2 244 240 2 x 120 SSD; deprecated"),
    D22xlarge(InstanceType.D22xlarge, 8, 61, 12000, "Intel Xeon E5-2676 v3 2.4 GHz; 6 x 2000 HDD"),
    D24xlarge(InstanceType.D24xlarge, 16, 122, 24000, "Intel Xeon E5-2676 v3 2.4 GHz; 12 x 2000 HDD"),
    D28xlarge(InstanceType.D28xlarge, 36, 244, 48000, "Intel Xeon E5-2676 v3 2.4 GHz; 24 x 2000 HDD"),
    D2xlarge(InstanceType.D2Xlarge, 4, 30.5, 6000, "Intel Xeon E5-2676 v3 2.4 GHz; 3 x 2000 HDD"),
    F116xlarge(InstanceType.F116xlarge, 64, 976, 3840, "Intel Xeon E5-2686 v4 2.3 GHz; 4 x 960 HDD"),
    F12xlarge(InstanceType.F12xlarge, 8, 122, 480, "Intel Xeon E5-2686 v4 2.3 GHz; 1 x 480 SSD"),
    G22xlarge(InstanceType.G22xlarge, 8, 15, 60, "Intel Xeon E5-2670 2.6 GHz; 1 x 60 SSD"),
    G28xlarge(InstanceType.G28xlarge, 32, 60, 240, "Intel Xeon E5-2670 2.6 GHz; 2 x 120 SSD"),
    Hi14xlarge(InstanceType.Hi14xlarge, 16, 60.5, 2048, "2048 2 x 1,024 SSD; deprecated"),
    Hs18xlarge(InstanceType.Hs18xlarge, 16, 117, 48000, "24 x 2,000 HDD; deprecated"),
    I22xlarge(InstanceType.I22xlarge, 8, 61, 1600, "Intel Xeon E5-2670 v2 2.5 GHz; 2 x 800 SSD"),
    I24xlarge(InstanceType.I24xlarge, 16, 122, 3200, "Intel Xeon E5-2670 v2 2.5 GHz; 4 x 800 SSD"),
    I28xlarge(InstanceType.I28xlarge, 32, 244, 6400, "Intel Xeon E5-2670 v2 2.5 GHz; 8 x 800 SSD"),
    I2xlarge(InstanceType.I2Xlarge, 4, 30.5, 800, "Intel Xeon E5-2670 v2 2.5 GHz; 1 x 800 SSD"),
    M1large(InstanceType.M1Large, 2, 7.5, 840, "2 7.5 840 2 x 420 HDD; deprecated"),
    M1medium(InstanceType.M1Medium, 1, 3.75, 410, "1medium 1 3.75 410 1 x 410 HDD; deprecated"),
    M1small(InstanceType.M1Small, 1, 1.7, 160, "1small 1 1.7 160 1 x 160 HDD; deprecated"),
    M1xlarge(InstanceType.M1Xlarge, 4, 15, 1680, "4 15 1680 4 x 420 HDD; deprecated"),
    M22xlarge(InstanceType.M22xlarge, 4, 34.2, 850, "1 x 850 HDD; deprecated"),
    M24xlarge(InstanceType.M24xlarge, 8, 68.4, 1680, "24xlarge 8 68.4 1680 2 x 840 HDD; deprecated"),
    M2xlarge(InstanceType.M2Xlarge, 2, 17.1, 420, "17.1 420  1 x 420 HDD; deprecated"),
    M32xlarge(InstanceType.M32xlarge, 8, 30, 160, "Intel Xeon E5-2670 v2 2.5 GHz; 2 x 80 SSD"),
    M3large(InstanceType.M3Large, 2, 7.5, 32, "Intel Xeon E5-2670 v2 2.5 GHz; 1 x 32 SSD"),
    M3medium(InstanceType.M3Medium, 1, 3.75, 4, "Intel Xeon E5-2670 v2 2.5 GHz; 1 x 4 SSD"),
    M3xlarge(InstanceType.M3Xlarge, 4, 15, 80, "Intel Xeon E5-2670 v2 2.5 GHz; 2 x 40 SSD"),
    M410xlarge(InstanceType.M410xlarge, 40, 160, 0, "Intel Xeon E5-2676 v3 2.4 GHz"),
    M416xlarge(InstanceType.M416xlarge, 64, 256, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    M42xlarge(InstanceType.M42xlarge, 8, 32, 0, "Intel Xeon E5-2676 v3 2.4 GHz"),
    M44xlarge(InstanceType.M44xlarge, 16, 64, 0, "Intel Xeon E5-2676 v3 2.4 GHz"),
    M4large(InstanceType.M4Large, 2, 8, 0, "Intel Xeon E5-2676 v3 2.4 GHz"),
    M4xlarge(InstanceType.M4Xlarge, 4, 16, 0, "Intel Xeon E5-2676 v3 2.4 GHz"),
    P216xlarge(InstanceType.P216xlarge, 64, 732, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    P28xlarge(InstanceType.P28xlarge, 32, 488, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    P2xlarge(InstanceType.P2Xlarge, 4, 61, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    R32xlarge(InstanceType.R32xlarge, 8, 61, 160, "Intel Xeon E5-2670 v2 2.5 GHz; 1 x 160 SSD"),
    R34xlarge(InstanceType.R34xlarge, 16, 122, 320, "Intel Xeon E5-2670 v2 2.5 GHz; 1 x 320 SSD"),
    R38xlarge(InstanceType.R38xlarge, 32, 244, 640, "Intel Xeon E5-2670 v2 2.5 GHz; 2 x 320 SSD"),
    R3large(InstanceType.R3Large, 2, 15.25, 32, "Intel Xeon E5-2670 v2 2.5 GHz; 1 x 32 SSD"),
    R3xlarge(InstanceType.R3Xlarge, 4, 30.5, 80, "Intel Xeon E5-2670 v2 2.5 GHz; 1 x 80 SSD"),
    R416xlarge(InstanceType.R416xlarge, 64, 488, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    R42xlarge(InstanceType.R42xlarge, 8, 61, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    R44xlarge(InstanceType.R44xlarge, 16, 122, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    R48xlarge(InstanceType.R48xlarge, 32, 244, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    R4large(InstanceType.R4Large, 2, 15.25, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    R4xlarge(InstanceType.R4Xlarge, 4, 30.5, 0, "Intel Xeon E5-2686 v4 2.3 GHz"),
    T1micro(InstanceType.T1Micro, 1, 0.613, 0, "EBS only"),
    T22xlarge(InstanceType.T22xlarge, 8, 32, 0, "Intel Xeon 3.0 GHz"),
    T2large(InstanceType.T2Large, 2, 8, 0, "Intel Xeon 3.0 GHz"),
    T2medium(InstanceType.T2Medium, 2, 4, 0, "Intel Xeon 3.3 GHz"),
    T2micro(InstanceType.T2Micro, 1, 1, 0, "Intel Xeon 3.3 GHz"),
    T2nano(InstanceType.T2Nano, 1, 0.5, 0, "Intel Xeon 3.3 GHz"),
    T2small(InstanceType.T2Small, 1, 2, 0, "Intel Xeon 3.3 GHz"),
    T2xlarge(InstanceType.T2Xlarge, 4, 16, 0, "Intel Xeon 3.0 GHz"),
    X116large(InstanceType.X116xlarge, 64, 976, 1920, "Intel Xeon E7-8880 v3 2.3 GHz; 1 x 1,920 SSD"),
    X132xlarge(InstanceType.X132xlarge, 128, 1952, 3840, "Intel Xeon E7-8880 v3 2.3 GHz; 2 x 1,920 SSD");

    private final InstanceType type;
    private final int vcpus;
    private final double ram;
    private final int disk;
    private final String notes;

    AvailableFlavor(InstanceType type, int vcpus, double ram, int disk, String notes) {
        this.type = type;
        this.vcpus = vcpus;
        this.ram = ram;
        this.disk = disk;
        this.notes = notes;
    }

    public Flavor toFlavor() {
        Flavor flavor = new Flavor();
        flavor.setName(type.toString());
        flavor.setVcpus(vcpus);
        flavor.setRam(ram);
        flavor.setRootDisk(disk);
        flavor.setNotes(notes);
        flavor.setIsPublic(true);
        return flavor;
    }

}
