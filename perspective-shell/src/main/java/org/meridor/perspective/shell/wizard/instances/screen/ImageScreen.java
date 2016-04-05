package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.request.RequestProvider;
import org.meridor.perspective.shell.request.FindProjectsRequest;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.result.FindProjectsResult;
import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.ImageStep;
import org.meridor.perspective.shell.wizard.instances.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
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
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        imageStep.setProjectName(projectName);
        return imageStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        Optional<FindProjectsResult> maybeProject = getProject(projectName);
        if (maybeProject.isPresent()) {
            switch (maybeProject.get().getCloudType()) {
                case OPENSTACK: return Optional.of(flavorScreen);
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
