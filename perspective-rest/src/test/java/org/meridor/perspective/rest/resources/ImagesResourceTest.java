package org.meridor.perspective.rest.resources;

import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.client.ImagesApi;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class ImagesResourceTest extends BaseResourceTest<ImagesApi> {

    @Autowired
    private ImagesAware imagesAware;
    
    @Before
    public void before() {
        imagesAware.saveImage(EntityGenerator.getImage());
    }
    
    @Test
    public void testGetById() throws Exception {
        Thread.sleep(500);
        String imageId = EntityGenerator.getImage().getId();
        Response<Image> response = getApi().getById(imageId).execute();
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.body(), equalTo(EntityGenerator.getImage()));
    }

    @Test
    public void testGetByMissingId() throws Exception {
        Response<Image> response = getApi().getById("missing-id").execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    public void testSaveImages() throws Exception {
        List<Image> images = new ArrayList<>();
        Image image = EntityGenerator.getImage();
        image.setId("another-id");
        images.add(image);
        assertThat(imagesAware.getImages(), hasSize(1));
        Response<ResponseBody> response = getApi().add(images).execute();
        assertThat(response.isSuccessful(), is(true));
        assertThat(imagesAware.getImages(), hasSize(2));
    }

    @Test
    public void testDeleteExistingImages() throws Exception {
        String imageId = EntityGenerator.getImage().getId();
        List<String> imageIds = new ArrayList<String>() {
            {
                add(imageId);
            }
        };
        assertThat(imagesAware.getImages(), hasSize(1));
        Response<ResponseBody> response = getApi().delete(imageIds).execute();
        assertThat(response.isSuccessful(), is(true));
    }

    @Override
    protected Class<ImagesApi> getApiClass() {
        return ImagesApi.class;
    }
}
