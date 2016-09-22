package org.meridor.perspective.shell.common.request;

import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class MockRequest implements Request<String> {
    
    @Required
    private String payload;
    
    @Override
    public String getPayload() {
        return payload;
    }

    void setPayload(String payload) {
        this.payload = payload;
    }
}
