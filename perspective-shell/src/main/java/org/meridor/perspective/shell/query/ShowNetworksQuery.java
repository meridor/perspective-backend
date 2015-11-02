package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Network;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.NETWORK_NAMES;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ShowNetworksQuery implements Query<Predicate<Network>> {
    
    @Filter(NETWORK_NAMES)
    private Set<String> names;
    
    public ShowNetworksQuery withNames(String name) {
        this.names = parseEnumeration(name);
        return this;
    }

    @Override
    public Predicate<Network> getPayload() {
        return getNetworkPredicate(Optional.ofNullable(names));
    }

    private Predicate<Network> getNetworkPredicate(Optional<Set<String>> name) {
        return network -> (!name.isPresent() || name.get().contains(network.getName()));
    }

}
