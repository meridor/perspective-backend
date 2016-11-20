package org.meridor.perspective.shell.interactive.wizard.instances.add;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.common.validator.TestRepository;
import org.meridor.perspective.shell.interactive.wizard.AbstractWizardTest;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.screen.ProjectScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.PROJECT;

@Component
public abstract class AbstractAddInstancesTest extends AbstractWizardTest<AddInstancesWizard> {

    @Autowired
    private AddInstancesWizard addInstancesWizard;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private AnswersStorage answersStorage;

    protected AbstractAddInstancesTest(List<String> answers, boolean result, String command) {
        super(answers, result, command);
    }

    @Override
    protected AddInstancesWizard getWizard() {
        return addInstancesWizard;
    }

    protected abstract CloudType getCloudType();

    @Override
    protected void beforeInjectingAnswers() {
        testRepository.setCloudType(getCloudType());
    }

    @Override
    protected void beforeInjectingAnswer(Class<? extends WizardScreen> cls) {
        if (ProjectScreen.class.isAssignableFrom(cls)) {
            FindProjectsResult project = testRepository.findProjects(null).get(0);
            answersStorage.put(ProjectStep.class, PROJECT, project);
        }
    }

}
