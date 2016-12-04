package org.meridor.perspective.shell.interactive.wizard.common.step;

import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.FindFlavorsRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindFlavorsResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.ChoicesStorage;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.FLAVOR;
import static org.meridor.perspective.shell.interactive.wizard.AnswersStorageUtils.getProjectName;

@Component
public abstract class AbstractFlavorStep extends SingleChoiceStep<FindFlavorsResult> {

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private RequestProvider requestProvider;

    @Override
    protected List<FindFlavorsResult> getPossibleChoices(AnswersStorage previousAnswers) {
        return projectsRepository.findFlavors(
                requestProvider.get(FindFlavorsRequest.class).withProjects(getProjectName(getProjectStepClass(), previousAnswers))
        );
    }

    protected abstract Class<? extends AbstractProjectStep> getProjectStepClass();

    @Override
    protected Function<FindFlavorsResult, String> getAnswerProvider() {
        return FindFlavorsResult::getName;
    }

    @Override
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<FindFlavorsResult> choicesStorage, String answer) {
        FindFlavorsResult flavor = choicesStorage.getChoicesMap().get(Integer.parseUnsignedInt(answer));
        answersStorage.put(getClass(), FLAVOR, flavor);
    }
}
