package org.meridor.perspective.shell.interactive.wizard.common.step;

import org.meridor.perspective.shell.common.repository.InstancesRepository;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindInstancesResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.ChoicesStorage;
import org.meridor.perspective.shell.interactive.wizard.MultipleChoicesStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.parseRange;
import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.INSTANCES;
import static org.meridor.perspective.shell.interactive.wizard.AnswersStorageUtils.getProjectName;

@Component
public abstract class AbstractMultipleInstancesStep extends MultipleChoicesStep<FindInstancesResult> {

    @Autowired
    private InstancesRepository instancesRepository;

    @Autowired
    private RequestProvider requestProvider;

    @Override
    protected List<FindInstancesResult> getPossibleChoices(AnswersStorage previousAnswers) {
        return instancesRepository.findInstances(
                requestProvider.get(FindInstancesRequest.class)
                        .withProjectNames(getProjectName(getProjectStepClass(), previousAnswers))
        );
    }

    protected Class<? extends AbstractProjectStep> getProjectStepClass() {
        return null;
    }

    @Override
    protected Function<FindInstancesResult, String> getAnswerProvider() {
        return FindInstancesResult::getName;
    }

    @Override
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<FindInstancesResult> choicesStorage, String answer) {
        Set<Integer> range = parseRange(answer);
        List<FindInstancesResult> instances = range.stream().
                map(i -> choicesStorage.getChoicesMap().get(i))
                .collect(Collectors.toList());
        answersStorage.put(getClass(), INSTANCES, instances);
    }
}
