package org.meridor.perspective.rest.resources;

import org.junit.Before;
import org.junit.Test;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ImagesResourceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ImagesAware imagesAware;
    
    @Before
    public void before() {
        imagesAware.saveImage(EntityGenerator.getImage());
    }
    
    @Test
    public void testGetById() throws Exception {
        Thread.sleep(500);
        Response response = target("/images/test-image")
                .request()
                .get();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void testSaveImages() throws Exception {
        List<Image> images = new ArrayList<>();
        Image instance = EntityGenerator.getImage();
        images.add(instance);
        Entity<List<Image>> entity = Entity.entity(images, MediaType.APPLICATION_JSON);
        Response response = target("/images")
                .request()
                .post(entity);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void testDeleteExistingImages() throws Exception {
        deleteImage(EntityGenerator.getImage());
    }

    private void deleteImage(Image image) throws Exception {
        List<String> images = new ArrayList<String>() {
            {
                add(image.getId());
            }
        };
        Entity<List<String>> entity = Entity.entity(images, MediaType.APPLICATION_JSON_TYPE);
        Response deleteResponse = target("/images/delete")
                .request()
                .post(entity);

        assertThat(deleteResponse.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

}
