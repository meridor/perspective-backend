package org.meridor.perspective.shell.interactive;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public final class PathUtils {
    
    public static Path getConfigurationDirectoryPath() {
        Path homeDirectory = Paths.get(System.getProperty("user.home"));
        Path defaultConfigurationDirectory = homeDirectory.resolve(".perspective");
        Optional<Path> configurationDirectoryCandidate = Stream.of(
                homeDirectory.resolve(".config").resolve("perspective"),
                defaultConfigurationDirectory
        )
                .filter(p -> Files.exists(p))
                .findFirst();
        return 
                configurationDirectoryCandidate.isPresent() ?
                    configurationDirectoryCandidate.get() :
                    defaultConfigurationDirectory;
    }
    
    public static Path getConfigurationFilePath(String name) {
        return getConfigurationDirectoryPath().resolve(name);
    }
    
}
