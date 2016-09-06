package org.meridor.perspective.worker.operation.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.misc.impl.MockCloud;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.config.OperationType.*;
import static org.meridor.perspective.worker.operation.impl.MockOperationsAware.TEST_STRING;

@ContextConfiguration(locations = "/META-INF/spring/operation-processor-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class OperationProcessorImplTest {

    private static final Cloud CLOUD = new MockCloud();
    
    @Autowired
    private OperationProcessor operationProcessor;
    
    @Test(expected = IllegalArgumentException.class)
    public void testNoAction() {
        operationProcessor.consume(CLOUD, LIST_INSTANCES, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNoOperation() {
        operationProcessor.<String>consume(CLOUD, LIST_PROJECTS, anything -> {});
    }
    
    @Test
    public void testConsume() {
        List<String> results = new ArrayList<>();
        assertThat(
                operationProcessor.<String>consume(CLOUD, LIST_INSTANCES, results::add),
                is(true)
        );
        assertThat(
                operationProcessor.<String>consume(
                        CLOUD,
                        LIST_INSTANCES,
                        Collections.singleton("some-id"),
                        results::add
                ),
                is(true)
        );
        assertThat(results, contains(TEST_STRING, TEST_STRING));
    }
    
    @Test
    public void testConsumeNotSupplyingOperation() {
        assertThat(
                operationProcessor.consume(CLOUD, ADD_INSTANCE, anything -> {}),
                is(false)
        );
    }
    
    @Test
    public void testSupply() {
        assertThat(
                operationProcessor.supply(CLOUD, ADD_INSTANCE, () -> TEST_STRING),
                is(true)
        );
    }

    @Test
    public void testSupplyNotConsumingOperation() {
        assertThat(
                operationProcessor.supply(CLOUD, LIST_INSTANCES, () -> TEST_STRING),
                is(false)
        );
    }
    
    @Test
    public void testProcess() {
        assertThat(
                operationProcessor.process(CLOUD, DELETE_INSTANCE, () -> TEST_STRING),
                equalTo(Optional.of(TEST_STRING))
        );
    }

    @Test
    public void testProcessNotProcessingOperation() {
        assertThat(
                operationProcessor.process(CLOUD, ADD_INSTANCE, () -> TEST_STRING),
                equalTo(Optional.empty())
        );
    }
    
}