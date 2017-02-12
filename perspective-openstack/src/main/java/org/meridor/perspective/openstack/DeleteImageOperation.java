package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.DELETE_IMAGE;

@Component
public class DeleteImageOperation extends BaseImageOperation {

    @Override
    protected BiFunction<Api, Image, Boolean> getAction() {
        return (api, image) -> api.deleteImage(image.getRealId());
    }

    @Override
    protected String getSuccessMessage(Image entity) {
        return "Deleted image %s (%s)";
    }

    @Override
    protected String getErrorMessage(Image entity) {
        return "Failed to delete image %s (%s)";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{DELETE_IMAGE};
    }

}
