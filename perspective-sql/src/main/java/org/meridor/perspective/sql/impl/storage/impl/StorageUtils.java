package org.meridor.perspective.sql.impl.storage.impl;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class StorageUtils {

    private static final String COMPOSITE_ID_DELIMITER = ":";

    public static String createCompositeId(String... pieces) {
        return Arrays.stream(pieces).collect(Collectors.joining(COMPOSITE_ID_DELIMITER));
    }

    public static String[] parseCompositeId(String id, int requiredSize) {
        String[] ret = id != null ? id.split(COMPOSITE_ID_DELIMITER) : new String[0];
        Assert.isTrue(ret.length == requiredSize, String.format("ID %s should contain %d independent pieces", id, requiredSize));
        return ret;
    }

    private StorageUtils(){
        
    }
    
}
