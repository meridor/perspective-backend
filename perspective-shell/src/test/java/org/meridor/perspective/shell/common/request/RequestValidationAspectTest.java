package org.meridor.perspective.shell.common.request;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RequestValidationAspectTest {
    
    @Autowired
    private RequestProvider requestProvider;
    
    @Test(expected = InvalidRequestException.class)
    public void testInvalidRequest() {
        requestProvider.get(MockRequest.class).getPayload();
    }
    
    @Test
    public void testValidRequest() {
        MockRequest mockRequest = requestProvider.get(MockRequest.class);
        mockRequest.setPayload("test");
        assertThat(mockRequest.getPayload(), equalTo("test"));
    }
    
    
}