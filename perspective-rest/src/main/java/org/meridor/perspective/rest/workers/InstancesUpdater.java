package org.meridor.perspective.rest.workers;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.InstancesDeletingEvent;
import org.meridor.perspective.events.InstancesEvent;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.impl.FSMBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstancesUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);
    
    @Autowired
    private OperationProcessor operationProcessor;
    
    @Autowired
    private Storage storage;
    
    @Handler
    public void updateInstances(@Body InstancesEvent instancesEvent) {
        Yatomata<InstancesFSM> fsm = new FSMBuilder<>(InstancesFSM.class).build();
    }

    
}
