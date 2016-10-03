package org.meridor.perspective.shell.common.repository.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.ADD_IMAGE;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@ContextConfiguration(locations = "/META-INF/spring/repository-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = AFTER_CLASS)
public class ServiceRepositoryImplTest {

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    public void testGetSupportedOperations() {
        assertThat(
                serviceRepository.getSupportedOperations(),
                equalTo(Collections.singletonMap(MOCK, Collections.singleton(ADD_IMAGE)))
        );
    }
    
}