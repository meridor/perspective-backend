package org.meridor.perspective.shell.common.repository;

import org.meridor.perspective.client.ImagesApi;
import org.meridor.perspective.client.InstancesApi;
import org.meridor.perspective.client.QueryApi;
import org.meridor.perspective.shell.common.misc.HumanReadableException;

import java.util.concurrent.Callable;

public interface ApiProvider {

    String API_SYSTEM_PROPERTY = "perspective.api.url";

    InstancesApi getInstancesApi();

    ImagesApi getImagesApi();

    QueryApi getQueryApi();

    String getBaseUri();

    static <T> T processRequestOrException(Callable<T> action) {
        try {
            return action.call();
        } catch (Exception e) {
            throw new HumanReadableException(e);
        }
    }
}
