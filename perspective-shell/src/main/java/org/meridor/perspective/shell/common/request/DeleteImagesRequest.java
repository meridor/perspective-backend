package org.meridor.perspective.shell.common.request;

import org.meridor.perspective.shell.common.repository.ImagesRepository;
import org.meridor.perspective.shell.common.result.FindImagesResult;
import org.meridor.perspective.shell.common.validator.Field;
import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.meridor.perspective.shell.common.validator.annotation.SupportedCloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class DeleteImagesRequest implements Request<List<String>> {

    @Autowired
    private ImagesRepository imagesRepository;

    @Filter(Field.IMAGE_NAMES)
    @Required
    private String names;
    
    @Filter(Field.CLOUDS)
    @SupportedCloud
    private String clouds;

    @Autowired
    private RequestProvider requestProvider;

    public DeleteImagesRequest withNames(String names) {
        this.names = names;
        return this;
    }
    
    public DeleteImagesRequest withClouds(String clouds) {
        this.clouds = clouds;
        return this;
    }

    @Override
    public List<String> getPayload() {
        return imagesRepository.findImages(requestProvider.get(FindImagesRequest.class)
                    .withNames(names)
                    .withClouds(clouds))
                .stream().map(FindImagesResult::getId)
                .collect(Collectors.toList());
    }
}
