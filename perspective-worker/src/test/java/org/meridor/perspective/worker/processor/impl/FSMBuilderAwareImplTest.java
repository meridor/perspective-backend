package org.meridor.perspective.worker.processor.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.worker.processor.FSMBuilderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = "/META-INF/spring/fsm-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class FSMBuilderAwareImplTest {

    @Autowired
    private FSMBuilderAware fsmBuilderAware;
    
    @Test(expected = RuntimeException.class)
    public void testMissingFSMClass() {
        fsmBuilderAware.get(String.class).build("anything");
    }
    
}