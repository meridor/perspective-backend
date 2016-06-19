package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.parser.DataSource;

import java.util.Map;

public interface DataSourceStrategy {
    
    DataContainer process(DataSource dataSource, Map<String, String> tableAliases);
    
}
