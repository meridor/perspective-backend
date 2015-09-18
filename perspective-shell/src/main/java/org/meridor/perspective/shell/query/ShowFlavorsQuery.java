package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.shell.validator.Filter;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.FLAVOR_NAMES;

public class ShowFlavorsQuery implements Query<Predicate<Flavor>> {
    
    @Filter(FLAVOR_NAMES)
    private Set<String> names;
    
    public ShowFlavorsQuery(String name) {
        this.names = parseEnumeration(name);
    }

    @Override
    public Predicate<Flavor> getPayload() {
        return getFlavorPredicate(Optional.ofNullable(names));
    }

    private Predicate<Flavor> getFlavorPredicate(Optional<Set<String>> names) {
        return flavor -> (!names.isPresent() || names.get().contains(flavor.getName()));
    }

}
