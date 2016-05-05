package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

@Component
@Index(columnNames = "str", length = 2)
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
