package org.meridor.perspective.shell.common.repository.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.common.repository.ImagesRepository;
import org.meridor.perspective.shell.common.request.AddImagesRequest;
import org.meridor.perspective.shell.common.request.FindImagesRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindImagesResult;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.MockQueryResults.createMockImagesResult;
import static org.meridor.perspective.shell.common.repository.impl.MockQueryResults.createMockInstancesResult;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@ContextConfiguration(locations = "/META-INF/spring/repository-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = AFTER_CLASS)
public class ImagesRepositoryImplTest {

    @Autowired
    private MockQueryRepository mockQueryRepository;

    @Autowired
    private RequestProvider requestProvider;

    @Autowired
    private ImagesRepository imagesRepository;

    @Test
    public void testFindImages() {
        Project project = EntityGenerator.getProject();
        Image image = EntityGenerator.getImage();
        QueryResult queryResult = createMockImagesResult(project, image);
        mockQueryRepository.addQueryResult(queryResult);
        FindImagesRequest findImagesRequest = requestProvider.get(FindImagesRequest.class);
        List<FindImagesResult> images = imagesRepository.findImages(findImagesRequest);
        assertThat(images, hasSize(1));
        FindImagesResult result = images.get(0);
        assertThat(result.getId(), equalTo(image.getId()));
        assertThat(result.getRealId(), equalTo(image.getRealId()));
        assertThat(result.getName(), equalTo(image.getName()));
        assertThat(result.getCloudType(), equalTo(image.getCloudType()));
        assertThat(result.getState(), equalTo(image.getState().value()));
        assertThat(result.getLastUpdated(), is(notNullValue()));
        assertThat(result.getProjectIds(), contains(project.getId()));
        assertThat(result.getProjectNames(), contains(project.getName()));
    }

    @Test
    public void testAddImages() {
        testAddImagesByName("something");
    }

    @Test
    public void testAddImagesDefaultName() {
        testAddImagesByName(null);
    }

    private void testAddImagesByName(String imageName) {
        Project project = EntityGenerator.getProject();
        Instance instance = EntityGenerator.getInstance();
        Image image = EntityGenerator.getImage();
        Flavor flavor = EntityGenerator.getFlavor();
        QueryResult queryResult = createMockInstancesResult(project, instance, image, flavor);
        mockQueryRepository.addQueryResult(queryResult);
        AddImagesRequest addImagesRequest = requestProvider.get(AddImagesRequest.class)
                .withInstanceNames(instance.getName());
        if (imageName != null) {
            addImagesRequest = addImagesRequest.withName(imageName);
        }
        List<Image> images = addImagesRequest.getPayload();
        assertThat(imagesRepository.addImages(images), is(empty()));
    }

    @Test
    public void testDeleteImages() {
        assertThat(imagesRepository.deleteImages(Collections.singleton("test-id")), is(empty()));
    }

}