package org.meridor.perspective.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MockWorker {

    private static final Logger LOG = LoggerFactory.getLogger(MockWorker.class);
    
    public static void main(String[] args) {
        LOG.info("Starting mock worker process");
        AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/spring/mocked-storage-context.xml");
        applicationContext.registerShutdownHook();
        applicationContext.start();
    }
    
}
