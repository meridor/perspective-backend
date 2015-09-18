package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.Required;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;

public class ModifyInstancesQuery implements Query<List<Instance>> {

    private InstancesRepository instancesRepository;

    @Filter(Field.INSTANCE_NAMES)
    @Required
    private Set<String> names;
    
    @Filter(Field.CLOUDS)
    @SupportedCloud
    private String cloud;

    public ModifyInstancesQuery(String names, String cloud, InstancesRepository instancesRepository) {
        this.names = parseEnumeration(names);
        this.cloud = cloud;
        this.instancesRepository = instancesRepository;
    }

    @Override
    public List<Instance> getPayload() {
        return names.stream().flatMap(t -> {
            ShowInstancesQuery showInstancesQuery = new ShowInstancesQuery(t, t, cloud);
            return instancesRepository.showInstances(showInstancesQuery).stream();
        }).collect(Collectors.toList());
    }
}
