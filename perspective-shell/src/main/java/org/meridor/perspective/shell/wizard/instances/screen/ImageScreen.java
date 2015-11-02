package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ProjectsRepository;
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

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        imageStep.setProjectName(projectName);
        return imageStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        Optional<Project> maybeProject = getProject(projectName);
        if (maybeProject.isPresent()) {
            switch (maybeProject.get().getCloudType()) {
                case OPENSTACK: return Optional.of(flavorScreen);
                case DOCKER: return Optional.of(commandScreen);
                default: return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Optional<Project> getProject(String projectName) {
        List<Project> projects = projectsRepository.showProjects(new ShowProjectsQuery().withNames(projectName));
        return ((projects.size() >= 1)) ?
                Optional.of(projects.get(0)) :
                Optional.empty();
                
    }
}
