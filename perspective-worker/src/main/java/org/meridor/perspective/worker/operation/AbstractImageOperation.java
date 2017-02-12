package org.meridor.perspective.worker.operation;

import org.meridor.perspective.beans.Image;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractImageOperation<T> extends AbstractEntityOperation<T, Image> {

    @Override
    protected String getEntityRealId(Image image) {
        return image.getRealId();
    }

}
