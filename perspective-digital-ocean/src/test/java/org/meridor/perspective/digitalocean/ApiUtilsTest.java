package org.meridor.perspective.digitalocean;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.digitalocean.ApiUtils.list;

public class ApiUtilsTest {

    @Test
    public void testList() {
        
        List<String> result = list(pn -> {
            if (pn == 1) {
                return Collections.singletonList("test");
            }
            return Collections.emptyList();
        });
        assertThat(result, contains("test"));
        
    }
    
}