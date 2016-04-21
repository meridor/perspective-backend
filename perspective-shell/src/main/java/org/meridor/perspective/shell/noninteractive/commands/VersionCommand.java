package org.meridor.perspective.shell.noninteractive.commands;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.getVersion;

public class VersionCommand implements Command {
    @Override
    public void run() {
        System.out.println(getVersion());
    }
}
