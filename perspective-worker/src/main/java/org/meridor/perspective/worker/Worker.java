package org.meridor.perspective.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Worker {

    private static Logger LOG = LoggerFactory.getLogger(Worker.class);

    public static void main(String[] args) {
        LOG.info("Starting worker process");
        AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/spring/context.xml");
        applicationContext.start();
    }

}
