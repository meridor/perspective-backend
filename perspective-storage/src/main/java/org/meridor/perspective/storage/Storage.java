package org.meridor.perspective.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Storage {

    private static final Logger LOG = LoggerFactory.getLogger(Storage.class);

    public static void main(String[] args) {
        LOG.info("Starting storage");
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/context.xml");
        context.start();
    }

}
