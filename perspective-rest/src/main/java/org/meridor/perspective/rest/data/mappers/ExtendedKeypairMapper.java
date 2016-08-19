package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ExtendedKeypair;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;

@Component
public class ExtendedKeypairMapper extends BaseObjectMapper<ExtendedKeypair> {
    @Override
    protected Map<String, Function<ExtendedKeypair, Object>> getColumnMapping() {
        return new HashMap<String, Function<ExtendedKeypair, Object>>() {
            {
                put("project_id", ExtendedKeypair::getProjectId);
                put("name", ExtendedKeypair::getName);
                put("fingerprint", ExtendedKeypair::getFingerprint);
                put("public_key", ExtendedKeypair::getPublicKey);
            }
        };
    }

    @Override
    public Class<ExtendedKeypair> getInputClass() {
        return ExtendedKeypair.class;
    }

    @Override
    public String getId(ExtendedKeypair keypair) {
        return createCompositeId(keypair.getProjectId(), keypair.getName());
    }
}
