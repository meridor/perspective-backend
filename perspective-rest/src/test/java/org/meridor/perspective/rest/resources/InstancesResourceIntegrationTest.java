package org.meridor.perspective.rest.resources;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;

public class InstancesResourceIntegrationTest {
    
    @Test
    public void testList() {
        get("/projects/test-project/regions/test-region/instances/list")
                .then()
                .assertThat()
                .statusCode(200);
    }
    
}
