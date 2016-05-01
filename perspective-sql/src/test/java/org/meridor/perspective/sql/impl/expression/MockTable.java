package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

@Component
public class MockTable implements Table {
    
    public String str;
    
    public Float num;
    
    public Integer numWithDefaultValue = 42;
    
    public Long missingDefaultValue;
    
    @Override
    public String getName() {
        return "mock";
    }
}
