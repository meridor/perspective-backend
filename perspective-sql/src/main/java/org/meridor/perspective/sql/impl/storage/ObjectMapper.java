package org.meridor.perspective.sql.impl.storage;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface ObjectMapper<T> {

    /**
     * Maps object to a list of column values
     *
     * @param input object to process
     * @return list of columns values
     */
    Map<String, Object> map(T input);

    /**
     * Returns a list of available column names
     *
     * @return a list of available column names
     */
    List<String> getAvailableColumnNames();

    /**
     * Returns allowed class for input object
     *
     * @return allowed class
     */
    Class<T> getInputClass();

    /**
     * Returns object id
     * @return object id
     */
    String getId(T input);

}
