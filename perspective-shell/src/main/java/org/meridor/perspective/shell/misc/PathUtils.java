package org.meridor.perspective.shell.misc;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathUtils {
    
    public static Path getConfigurationDirectoryPath() {
        return Paths.get(System.getProperty("user.home")).resolve(".perspective");
    }
    
    public static Path getConfigurationFilePath(String name) {
        return getConfigurationDirectoryPath().resolve(name);
    }
    
    private PathUtils() {}
    
}
