package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.LIST_IMAGES;

@Component
public class ListImagesOperation implements SupplyingOperation<Set<Image>> {

    @Autowired
    private ImagesStorage images;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Image>> consumer) {
        consumer.accept(images);
        return true;
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Set<Image>> consumer) {
        Set<Image> matchingImages = images.stream()
                .filter(i -> ids.contains(i.getRealId()))
                .collect(Collectors.toSet());
        consumer.accept(matchingImages);
        return true;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_IMAGES};
    }
}
