package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.springframework.stereotype.Component;

import java.util.HashSet;

import static org.meridor.perspective.mock.EntityGenerator.getInstance;

@Component
public class InstancesStorage extends HashSet<Instance> {

    public InstancesStorage() {
        add(getInstance());
    }
    
}
