package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.FindNetworksRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindNetworksResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.ChoicesStorage;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.NETWORK;
import static org.meridor.perspective.shell.interactive.wizard.AnswersStorageUtils.getProjectName;

@Component("addInstancesNetworkStep")
public class NetworkStep extends SingleChoiceStep<FindNetworksResult> {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private RequestProvider requestProvider;

    @Override
    protected List<FindNetworksResult> getPossibleChoices(AnswersStorage previousAnswers) {
        return projectsRepository.findNetworks(requestProvider.get(FindNetworksRequest.class)
                .withProjects(getProjectName(previousAnswers)));
    }

    @Override
    protected Function<FindNetworksResult, String> getAnswerProvider() {
        return FindNetworksResult::getName;
    }

    @Override
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<FindNetworksResult> choicesStorage, String answer) {
        FindNetworksResult network = choicesStorage.getChoicesMap().get(Integer.parseUnsignedInt(answer));
        answersStorage.put(getClass(), NETWORK, network);
    }

    @Override
    public String getMessage() {
        return "Select network to use for instances:";
    }
    
}
