package org.meridor.perspective.shell.common.impl;

import org.meridor.perspective.shell.common.misc.Logger;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

@Component
public class LoggerImpl implements Logger {

    private static final java.util.logging.Logger LOG = HandlerUtils.getLogger(Logger.class);
    
    @Override
    public void ok() {
        ok("OK");
    }

    @Override
    public void ok(String message) {
        LOG.info(message);
    }

    @Override
    public void warn(String message) {
        LOG.warning(message);
    }

    @Override
    public void error(String message) {
        LOG.severe(message);
    }
}
