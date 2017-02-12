package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ExtendedFlavor;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;

@Component
public class ExtendedFlavorMapper extends BaseObjectMapper<ExtendedFlavor> {
    @Override
    protected Map<String, Function<ExtendedFlavor, Object>> getColumnMapping() {
        return new HashMap<String, Function<ExtendedFlavor, Object>>() {
            {
                put("project_id", ExtendedFlavor::getProjectId);
                put("id", ExtendedFlavor::getId);
                put("name", ExtendedFlavor::getName);
                put("ram", ExtendedFlavor::getRam);
                put("vcpus", ExtendedFlavor::getVcpus);
                put("root_disk", ExtendedFlavor::getRootDisk);
                put("ephemeral_disk", ExtendedFlavor::getEphemeralDisk);
                put("has_swap", ExtendedFlavor::hasSwap);
                put("is_public", ExtendedFlavor::isPublic);
                put("notes", ExtendedFlavor::getNotes);
            }
        };
    }

    @Override
    public Class<ExtendedFlavor> getInputClass() {
        return ExtendedFlavor.class;
    }

    @Override
    public String getId(ExtendedFlavor flavor) {
        return createCompositeId(flavor.getProjectId(), flavor.getId());
    }
}
