package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.request.QueryProvider;
import org.meridor.perspective.shell.request.FindImagesRequest;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.result.FindImagesResult;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageStep extends SingleChoiceStep {
    
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
        return imagesRepository.findImages(queryProvider.get(FindImagesRequest.class).withProjects(projectName)).stream()
                .map(FindImagesResult::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select image to launch instances from:";
    }
    
}
