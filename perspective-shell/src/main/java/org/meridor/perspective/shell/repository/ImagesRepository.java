package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.query.AddImagesQuery;
import org.meridor.perspective.shell.query.DeleteImagesQuery;
import org.meridor.perspective.shell.query.ShowImagesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ImagesRepository {

    @Autowired
    private ApiProvider apiProvider;

    public List<Image> showImages(ShowImagesQuery showImagesQuery) {
        GenericType<ArrayList<Image>> instanceListType = new GenericType<ArrayList<Image>>() {};
        List<Image> images = apiProvider.getImagesApi().getAsXml(instanceListType);
        return images.stream().filter(showImagesQuery.getPayload()).collect(Collectors.toList());
    }

    public Set<String> addImages(AddImagesQuery addImagesQuery) {
        List<Image> images = addImagesQuery.getPayload();
        apiProvider.getImagesApi().postXmlAs(images, String.class);
        return Collections.emptySet();
    }
    
    public Set<String> deleteImages(DeleteImagesQuery deleteImagesQuery) {
        List<Image> images = deleteImagesQuery.getPayload();
        apiProvider.getInstancesApi().delete().postXmlAs(images, String.class);
        return Collections.emptySet();
    }

}
