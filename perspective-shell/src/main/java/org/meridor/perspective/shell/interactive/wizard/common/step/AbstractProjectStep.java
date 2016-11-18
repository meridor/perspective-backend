package org.meridor.perspective.shell.interactive.wizard.common.step;

import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.FindProjectsRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.ChoicesStorage;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.PROJECT;

@Component
public abstract class AbstractProjectStep extends SingleChoiceStep<FindProjectsResult> {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private RequestProvider requestProvider;
    
    @Override
    protected List<FindProjectsResult> getPossibleChoices(AnswersStorage previousAnswers) {
        return projectsRepository.findProjects(requestProvider.get(FindProjectsRequest.class));
    }

    @Override
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<FindProjectsResult> choicesStorage, String answer) {
        FindProjectsResult project = choicesStorage.getChoicesMap().get(Integer.parseUnsignedInt(answer));
        answersStorage.put(getClass(), PROJECT, project);
    }

    @Override
    protected Function<FindProjectsResult, String> getAnswerProvider() {
        return FindProjectsResult::getName;
    }
}
