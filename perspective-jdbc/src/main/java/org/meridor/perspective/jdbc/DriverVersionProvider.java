package org.meridor.perspective.jdbc;

import java.util.Optional;

public final class DriverVersionProvider {
    
    private static final Class<DriverVersionProvider> cls = DriverVersionProvider.class;
    
    public static String getVersion() {
        Optional<String> version = Optional.ofNullable(cls.getPackage().getImplementationVersion());
        return version.isPresent() ? version.get() : "0.0";
    }

    private static int getVersionAt(int position) {
        String[] splittedVersion = getVersion().split(".");
        if (splittedVersion.length < 2) {
            splittedVersion = new String[]{"0", "0"};
        }
        return Integer.parseInt(splittedVersion[position]);
    }

    public static int getMajorVersion() {
        return getVersionAt(0);
    }
    
    public static int getMinorVersion() {
        return getVersionAt(1);
    }
    
}
