package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class InstancesStorage extends HashSet<Instance> {

    public InstancesStorage() {
        add(EntityGenerator.getInstance());
        add(EntityGenerator.getErrorInstance());
    }

}
