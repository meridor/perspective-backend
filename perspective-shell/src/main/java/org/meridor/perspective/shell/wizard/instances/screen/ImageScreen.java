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
    public Step getStep() {
        return imageStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        String projectId = previousAnswers.get(ProjectStep.class);
        List<Project> projects = projectsRepository.showProjects(new ShowProjectsQuery(projectId));
        if (projects.size() == 1) {
            Project project = projects.get(0);
            switch (project.getCloudType()) {
                case OPENSTACK: return Optional.of(flavorScreen);
                case MOCK:
                case DOCKER: return Optional.of(commandScreen);
            }
        }
        return Optional.empty();
    }
}
