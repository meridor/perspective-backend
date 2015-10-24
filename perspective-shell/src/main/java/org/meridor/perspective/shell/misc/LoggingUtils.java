package org.meridor.perspective.shell.misc;

import org.springframework.shell.support.logging.HandlerUtils;

import java.util.logging.Logger;

public class LoggingUtils {

    private static final Logger LOG = HandlerUtils.getLogger(LoggingUtils.class);

    public static void ok() {
        ok("OK");
    }

    public static void ok(String message) {
        LOG.info(message);
    }

    public static void warn(String message) {
        LOG.warning(message);
    }

    public static void error(String message) {
        LOG.severe(message);
    }

}
