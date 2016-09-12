package org.meridor.perspective.shell.common.repository.impl;

import jline.console.ConsoleReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ConsoleUtils {

    public static ConsoleReader mockConsoleReader(String input) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        return new ConsoleReader(inputStream, System.out);
    }

}
