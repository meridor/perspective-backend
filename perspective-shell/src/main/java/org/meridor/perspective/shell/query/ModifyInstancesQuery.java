package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ModifyInstancesQuery implements Query<List<Instance>> {

    @Autowired
    private InstancesRepository instancesRepository;

    @Filter(Field.INSTANCE_NAMES)
    @Required
    private Set<String> names;
    
    @Filter(Field.CLOUDS)
    @SupportedCloud
    private String clouds;
    
    public ModifyInstancesQuery withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }

    public ModifyInstancesQuery withClouds(String clouds) {
        this.clouds = clouds;
        return this;
    }

    @Override
    public List<Instance> getPayload() {
        return names.stream().flatMap(n -> instancesRepository.showInstances(
                new ShowInstancesQuery().withNames(n).withClouds(clouds)
        ).stream()).collect(Collectors.toList());
    }
}
