package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_IMAGE;

@Component
public class AddImageOperation implements ConsumingOperation<Image> {

    private static final Logger LOG = LoggerFactory.getLogger(AddImageOperation.class);

    @Autowired
    private ImagesStorage images;

    @Override
    public boolean perform(Cloud cloud, Supplier<Image> supplier) {
        Image image = supplier.get();
        LOG.info("Adding image {}", image);
        return images.add(image);
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_IMAGE};
    }
}
