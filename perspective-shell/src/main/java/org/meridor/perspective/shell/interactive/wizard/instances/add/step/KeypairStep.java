package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.FindKeypairsRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindKeypairsResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.ChoicesStorage;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.KEYPAIR;
import static org.meridor.perspective.shell.interactive.wizard.AnswersStorageUtils.getProjectName;

@Component("addInstancesKeypairStep")
public class KeypairStep extends SingleChoiceStep<FindKeypairsResult> {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private RequestProvider requestProvider;

    @Override
    protected List<FindKeypairsResult> getPossibleChoices(AnswersStorage previousAnswers) {
        return projectsRepository.findKeypairs(requestProvider.get(FindKeypairsRequest.class)
                .withProjects(getProjectName(previousAnswers)));
    }

    @Override
    protected Function<FindKeypairsResult, String> getAnswerProvider() {
        return FindKeypairsResult::getName;
    }

    @Override
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<FindKeypairsResult> choicesStorage, String answer) {
        FindKeypairsResult keypair = choicesStorage.getChoicesMap().get(Integer.parseUnsignedInt(answer));
        answersStorage.put(getClass(), KEYPAIR, keypair);
    }

    @Override
    public String getMessage() {
        return "Select keypair to use for instances:";
    }

    @Override
    public boolean answerRequired() {
        return false;
    }
}
