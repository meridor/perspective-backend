package org.meridor.perspective.shell.common.misc.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.common.misc.OperationSupportChecker;
import org.meridor.perspective.shell.common.validator.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.config.CloudType.DOCKER;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.ADD_IMAGE;
import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class OperationSupportCheckerImplTest {

    @Autowired
    private OperationSupportChecker operationSupportChecker;

    @Autowired
    private TestRepository testRepository;

    @Before
    public void before() {
        testRepository.addSupportedOperations(MOCK, Collections.singleton(ADD_IMAGE));
    }

    @Test
    public void testIsOperationSupported() {
        assertThat(operationSupportChecker.isOperationSupported(MOCK, ADD_IMAGE), is(true));
        assertThat(operationSupportChecker.isOperationSupported(MOCK, ADD_INSTANCE), is(false));
        assertThat(operationSupportChecker.isOperationSupported(DOCKER, ADD_IMAGE), is(false));
    }

    @Test
    public void testFilter() {
        Instance firstInstance = EntityGenerator.getInstance();
        List<Instance> instances = new ArrayList<Instance>() {
            {
                add(firstInstance);
                Instance secondInstance = EntityGenerator.getInstance();
                secondInstance.setCloudType(DOCKER);
            }
        };
        Collection<Instance> filteredInstances = operationSupportChecker.filter(
                instances,
                Instance::getCloudType,
                ADD_IMAGE
        );
        assertThat(filteredInstances, contains(firstInstance));
    }

    @Test
    public void testRepositoryFailure() {
        testRepository.setFail(true);
        //There should be no exception
        assertThat(operationSupportChecker.isOperationSupported(MOCK, ADD_IMAGE), is(false));
    }
    
}