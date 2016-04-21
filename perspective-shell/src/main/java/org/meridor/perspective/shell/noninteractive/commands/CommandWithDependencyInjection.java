package org.meridor.perspective.shell.noninteractive.commands;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class CommandWithDependencyInjection implements Command {

    private final AbstractApplicationContext applicationContext;

    CommandWithDependencyInjection() {
        this.applicationContext = new ClassPathXmlApplicationContext("META-INF/spring/non-interactive-context.xml");
        applicationContext.registerShutdownHook();
        applicationContext.start();
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public abstract void run();
}
