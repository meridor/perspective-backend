package org.meridor.perspective.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Rest {

    private static final Logger LOG = LoggerFactory.getLogger(Rest.class);

    public static void main(String[] args) {
        LOG.info("Starting REST process");
        AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/spring/context.xml");
        applicationContext.registerShutdownHook();
        applicationContext.start();
    }

}
