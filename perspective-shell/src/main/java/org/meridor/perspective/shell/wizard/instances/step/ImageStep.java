package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.query.QueryProvider;
import org.meridor.perspective.shell.query.ShowImagesQuery;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.wizard.ChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageStep extends ChoiceStep {
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @Autowired
    private QueryProvider queryProvider;
    
    @Required
    private String projectName;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    protected List<String> getPossibleChoices() {
        return imagesRepository.showImages(queryProvider.get(ShowImagesQuery.class).withProjectNames(projectName)).stream()
                .map(Image::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select image to launch instances from:";
    }
    
}
