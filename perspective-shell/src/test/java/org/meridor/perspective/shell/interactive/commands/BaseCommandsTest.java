package org.meridor.perspective.shell.interactive.commands;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.request.InvalidRequestException;
import org.meridor.perspective.shell.common.request.Request;
import org.meridor.perspective.shell.interactive.TestLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class BaseCommandsTest {

    @Autowired
    private MockBaseCommands mockBaseCommands;
    
    @Autowired
    private TestLogger logger;
    
    @Test
    public void testOk() {
        mockBaseCommands.ok();
        assertThat(logger.getOkMessages(), contains("OK"));
    }
    
    @Test
    public void testOkWithMessage() {
        mockBaseCommands.ok("test");
        assertThat(logger.getOkMessages(), contains("test"));
    }
    
    @Test
    public void testWarn() {
        mockBaseCommands.warn("test");
        assertThat(logger.getWarnMessages(), contains("test"));
    }
    
    @Test
    public void testError() {
        mockBaseCommands.error("test");
        assertThat(logger.getErrorMessages(), contains("test"));
    }
    
    @Test
    public void testOkOrShowError() {
        mockBaseCommands.okOrShowErrors(Collections.emptySet());
        assertThat(logger.getOkMessages(), contains("OK"));
        mockBaseCommands.okOrShowErrors(new HashSet<>(Arrays.asList("one", "two")));
        assertThat(logger.getErrorMessages(), contains("one\ntwo"));
    }
    
    @Test
    public void testValidateExecuteShowResult() {
        List<String> results = new ArrayList<>();
        Request<String> request = () -> "test";
        mockBaseCommands.validateExecuteShowResult(request, r -> results.add(r.getPayload()));
        assertThat(results, contains("test"));
        mockBaseCommands.validateExecuteShowResult(request, r -> {
            throw new InvalidRequestException(new HashSet<>(Arrays.asList("one", "two")));
        });
        assertThat(logger.getErrorMessages(), contains("one\ntwo"));
    }
    
}