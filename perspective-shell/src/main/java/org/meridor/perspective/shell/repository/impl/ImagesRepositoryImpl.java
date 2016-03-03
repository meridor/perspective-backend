package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.QueryRepository;
import org.meridor.perspective.shell.request.AddImagesRequest;
import org.meridor.perspective.shell.request.FindImagesRequest;
import org.meridor.perspective.shell.result.FindImagesResult;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericEntity;
import java.util.*;

import static java.lang.String.valueOf;
import static org.meridor.perspective.sql.DataUtils.get;

@Repository
public class ImagesRepositoryImpl implements ImagesRepository {

    @Autowired
    private ApiProvider apiProvider;
    
    @Autowired
    private QueryRepository queryRepository;
    
    @Override 
    public List<FindImagesResult> findImages(FindImagesRequest findImagesRequest) {
        QueryResult networksResult = queryRepository.query(findImagesRequest.getPayload());
        Data data = networksResult.getData();
        Map<String, FindImagesResult> resultsMap = new HashMap<>();
        data.getRows().stream()
                .forEach(r -> {
                    String imageId = valueOf(get(data, r, "images.id"));
                    FindImagesResult findImagesResult = resultsMap.getOrDefault(imageId, new FindImagesResult(
                            valueOf(get(data, r, "images.id")),
                            valueOf(get(data, r, "images.real_id")),
                            valueOf(get(data, r, "images.name")),
                            valueOf(get(data, r, "images.cloud_type")),
                            valueOf(get(data, r, "images.state")),
                            valueOf(get(data, r, "images.last_updated"))
                    ));
                    String projectId = valueOf(get(data, r, "projects.id"));
                    String projectName = valueOf(get(data, r, "projects.name"));
                    findImagesResult.getProjectIds().add(projectId);
                    findImagesResult.getProjectNames().add(projectName);
                    resultsMap.put(imageId, findImagesResult);
                });
        return new ArrayList<>(resultsMap.values());
    }

    @Override 
    public Set<String> addImages(AddImagesRequest addImagesRequest) {
        List<Image> images = addImagesRequest.getPayload();
        GenericEntity<List<Image>> data = new GenericEntity<List<Image>>(images) {
        };
        apiProvider.getImagesApi().postXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    @Override 
    public Set<String> deleteImages(Collection<String> imageIds) {
        GenericEntity<List<String>> data = new GenericEntity<List<String>>(new ArrayList<>(imageIds)) {
        };
        apiProvider.getImagesApi().delete().postXmlAs(data, String.class);
        return Collections.emptySet();
    }

}
