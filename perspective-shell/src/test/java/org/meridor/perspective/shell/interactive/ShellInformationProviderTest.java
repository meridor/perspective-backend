package org.meridor.perspective.shell.interactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.shell.common.events.PromptChangedEvent;
import org.meridor.perspective.shell.common.validator.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class ShellInformationProviderTest {

    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private PromptProvider promptProvider;
    
    @Autowired
    private TestRepository testRepository;
    
    @Test
    public void testPromptChange(){
        testRepository.addLetter(EntityGenerator.getLetter());
        eventBus.fire(new PromptChangedEvent());
        assertThat(promptProvider.getPrompt(), equalTo("perspective[*][1]>"));
    }
    
}