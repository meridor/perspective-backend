package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.query.AddImagesQuery;
import org.meridor.perspective.shell.query.DeleteImagesQuery;
import org.meridor.perspective.shell.query.ShowImagesQuery;
import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ImagesRepositoryImpl implements ImagesRepository {

    @Autowired
    private ApiProvider apiProvider;

    @Override 
    public List<Image> showImages(ShowImagesQuery showImagesQuery) {
        GenericType<ArrayList<Image>> instanceListType = new GenericType<ArrayList<Image>>() {};
        List<Image> images = apiProvider.getImagesApi().getAsXml(instanceListType);
        return images.stream()
                .filter(showImagesQuery.getPayload())
                .sorted((i1, i2) -> Comparator.<String>naturalOrder().compare(i1.getName(), i2.getName()))
                .collect(Collectors.toList());
    }

    @Override 
    public Set<String> addImages(AddImagesQuery addImagesQuery) {
        List<Image> images = addImagesQuery.getPayload();
        GenericEntity<List<Image>> data = new GenericEntity<List<Image>>(images) {
        };
        apiProvider.getImagesApi().postXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    @Override 
    public Set<String> deleteImages(DeleteImagesQuery deleteImagesQuery) {
        List<Image> images = deleteImagesQuery.getPayload();
        GenericEntity<List<Image>> data = new GenericEntity<List<Image>>(images) {
        };
        apiProvider.getInstancesApi().delete().postXmlAs(data, String.class);
        return Collections.emptySet();
    }

}
