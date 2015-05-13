package org.meridor.perspective.rest.resources;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;

public class ProjectsResourceIntegrationTest {
    
    @Test
    public void testList() {
        get("/projects/list")
                .then()
                .assertThat()
                .statusCode(200);
    }
    
}
