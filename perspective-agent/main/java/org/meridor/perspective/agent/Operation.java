package org.meridor.perspective.agent;

import javax.ws.rs.core.Response;
import java.util.function.Function;

/**
 * Main interface to be implemented in provider plugins
 * @param <T>
 */
public interface Operation<T> extends Function<T, Response> {
    
    OperationType getType();
    
}
