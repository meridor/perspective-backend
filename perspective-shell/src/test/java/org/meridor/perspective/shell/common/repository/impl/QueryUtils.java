package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.Row;

import java.util.List;

import static org.meridor.perspective.sql.QueryStatus.SUCCESS;

final class QueryUtils {
    static QueryResult createQueryResult(
            List<String> columnNames,
            List<List<Object>> values
    ) {
        QueryResult queryResult = new QueryResult();
        queryResult.setStatus(SUCCESS);
        Data data = new Data();
        data.setColumnNames(columnNames);
        values.forEach(dr -> data.getRows().add(new Row(){
            {
                getValues().addAll(dr);
            }
        }));
        queryResult.setData(data);
        return queryResult;
    }
}
