package org.meridor.perspective.shell.interactive.wizard.common.step;

import org.meridor.perspective.shell.common.repository.ImagesRepository;
import org.meridor.perspective.shell.common.request.FindImagesRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindImagesResult;
import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public abstract class AbstractImageStep extends SingleChoiceStep {

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private RequestProvider requestProvider;

    @Required
    private String projectName;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    protected List<String> getPossibleChoices() {
        return imagesRepository.findImages(requestProvider.get(FindImagesRequest.class).withProjects(projectName)).stream()
                .map(FindImagesResult::getName)
                .collect(Collectors.toList());
    }

}
