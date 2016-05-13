package org.meridor.perspective.sql.impl.storage;

import java.util.Map;
import java.util.Set;

public interface ObjectMapper<T> {

    /**
     * Maps object to a list of column values
     *
     * @param input object to process
     * @return list of columns values
     */
    Map<String, Object> map(T input);

    /**
     * Returns a set of available column names
     *
     * @return a set of available column names
     */
    Set<String> getAvailableColumnNames();

    /**
     * Returns allowed class for input object
     *
     * @return allowed class
     */
    Class<T> getInputClass();

}
