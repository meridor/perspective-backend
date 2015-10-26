package org.meridor.perspective.shell.wizard.instances;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.query.ShowImagesQuery;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.wizard.ChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageStep extends ChoiceStep {
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    private final String projectId;

    public ImageStep(String projectId) {
        this.projectId = projectId;
    }

    @Override
    protected List<String> getPossibleChoices() {
        return imagesRepository.showImages(new ShowImagesQuery(projectId)).stream()
                .map(Image::getId)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select image to launch instances from.";
    }
    
}
