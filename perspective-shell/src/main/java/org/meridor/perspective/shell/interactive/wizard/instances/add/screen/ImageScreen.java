package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.FindProjectsRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ImageStep;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("addInstancesImageScreen")
public class ImageScreen implements WizardScreen {
    
    @Autowired
    private ImageStep imageStep;
    
    @Autowired
    private FlavorScreen flavorScreen;
    
    @Autowired
    private CommandScreen commandScreen;
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private RequestProvider requestProvider;

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        String projectName = previousAnswers.getAnswer(ProjectStep.class);
        imageStep.setProjectName(projectName);
        return imageStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        String projectName = previousAnswers.getAnswer(ProjectStep.class);
        Optional<FindProjectsResult> projectCandidate = getProject(projectName);
        if (projectCandidate.isPresent()) {
            switch (projectCandidate.get().getCloudType()) {
                case OPENSTACK:
                case DIGITAL_OCEAN:
                    return Optional.of(flavorScreen);
                case DOCKER: return Optional.of(commandScreen);
                default: return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Optional<FindProjectsResult> getProject(String projectName) {
        List<FindProjectsResult> projects = projectsRepository.findProjects(requestProvider.get(FindProjectsRequest.class).withNames(projectName));
        return (projects.size() >= 1) ?
                Optional.of(projects.get(0)) :
                Optional.empty();
                
    }
}
