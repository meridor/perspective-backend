package org.meridor.perspective.shell.repository.impl;

import okhttp3.ResponseBody;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
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
import retrofit2.Call;

import java.util.*;

import static org.meridor.perspective.shell.repository.ApiProvider.processRequestOrException;

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
                    ValueFormatter vf = new ValueFormatter(data, r);
                    String imageId = vf.getString("images.id");
                    FindImagesResult findImagesResult = resultsMap.getOrDefault(imageId, new FindImagesResult(
                            vf.getString("images.id"),
                            vf.getString("images.real_id"),
                            vf.getString("images.name"),
                            vf.getString("images.cloud_type"),
                            vf.getString("images.state"),
                            vf.getString("images.last_updated")
                    ));
                    String projectId = vf.getString("projects.id");
                    String projectName = vf.getString("projects.name");
                    findImagesResult.getProjectIds().add(projectId);
                    findImagesResult.getProjectNames().add(projectName);
                    resultsMap.put(imageId, findImagesResult);
                });
        return new ArrayList<>(resultsMap.values());
    }

    @Override 
    public Set<String> addImages(AddImagesRequest addImagesRequest) {
        return processRequestOrException(() -> {
            List<Image> images = addImagesRequest.getPayload();
            Call<Collection<Instance>> call = apiProvider.getImagesApi().add(images);
            call.execute();
            return Collections.emptySet();
        });
    }
    
    @Override 
    public Set<String> deleteImages(Collection<String> imageIds) {
        return processRequestOrException(() -> {
            Call<ResponseBody> call = apiProvider.getImagesApi().delete(imageIds);
            call.execute();
            return Collections.emptySet();
        });
    }

}
