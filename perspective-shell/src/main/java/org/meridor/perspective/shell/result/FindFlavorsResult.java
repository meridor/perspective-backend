package org.meridor.perspective.shell.result;

import org.meridor.perspective.beans.Flavor;

public class FindFlavorsResult {
    
    private final String id;
    private final String name;
    private final String projectName;
    private final String vcpus;
    private final String ram;
    private final String rootDisk;
    private final String ephemeralDisk;

    public FindFlavorsResult(String id, String name, String projectName, String vcpus, String ram, String rootDisk, String ephemeralDisk) {
        this.id = id;
        this.name = name;
        this.projectName = projectName;
        this.vcpus = vcpus;
        this.ram = ram;
        this.rootDisk = rootDisk;
        this.ephemeralDisk = ephemeralDisk;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVcpus() {
        return vcpus;
    }

    public String getRam() {
        return ram;
    }

    public String getRootDisk() {
        return rootDisk;
    }

    public String getEphemeralDisk() {
        return ephemeralDisk;
    }
    
    public Flavor toFlavor() {
        Flavor flavor = new Flavor();
        flavor.setId(getId());
        flavor.setName(getName());
        flavor.setVcpus(Integer.valueOf(getVcpus()));
        flavor.setRam(Integer.valueOf(getRam()));
        flavor.setRootDisk(Integer.valueOf(getRootDisk()));
        flavor.setEphemeralDisk(Integer.valueOf(getEphemeralDisk()));
        return flavor;
    }
}
