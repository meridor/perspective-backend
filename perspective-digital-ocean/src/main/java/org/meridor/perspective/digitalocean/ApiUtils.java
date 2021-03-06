package org.meridor.perspective.digitalocean;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.misc.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class ApiUtils {

    static <T> List<T> list(Function<Integer, List<T>> action) {
        return listImpl(1, action, new ArrayList<>());
    }

    private static <T> List<T> listImpl(int pageNo, Function<Integer, List<T>> action, List<T> alreadyFetchedEntities) {
        try {
            List<T> newEntities = action.apply(pageNo);
            if (newEntities.isEmpty()) {
                return alreadyFetchedEntities;
            }
            alreadyFetchedEntities.addAll(newEntities);
            return listImpl(pageNo + 1, action, alreadyFetchedEntities);
        } catch (Exception e) {
            return alreadyFetchedEntities;
        }
    }

    static String createFakeImageId(IdGenerator idGenerator, Cloud cloud, String imageName) {
        return idGenerator.getImageId(cloud, imageName);
    }
    
}
