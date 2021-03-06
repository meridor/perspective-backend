package org.meridor.perspective.worker.misc;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.Config;
import org.meridor.perspective.worker.misc.impl.LimitedSizeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {

    private final WorkerMetadata workerMetadata;

    private final Config config;

    private LimitedSizeMap<String, String> idCache;

    @Autowired
    public IdGenerator(WorkerMetadata workerMetadata, Config config) {
        this.workerMetadata = workerMetadata;
        this.config = config;
    }

    public String generate(Class<?> role, String id) {
        String cloudId = workerMetadata.getId();
        String idRole = role.getCanonicalName();
        String seed = String.format("%s-%s-%s", cloudId, idRole, id);
        return getOrGenerate(seed);
    }
    
    private String getOrGenerate(String seed) {
        if (idCache == null) {
            idCache = new LimitedSizeMap<>(config.getIdCacheSize());
        }
        if (idCache.containsKey(seed)) {
            return idCache.get(seed);
        }
        String uuid = UUID.nameUUIDFromBytes(seed.getBytes()).toString();
        idCache.put(seed, uuid);
        return uuid;
    }

    public String getProjectId(Cloud cloud, String trait) {
        return generate(Project.class, String.format("%s-%s", cloud.getId(), trait));
    }
    
    public String getProjectId(Cloud cloud) {
        return getProjectId(cloud, "");
    }
    
    public String getInstanceId(Cloud cloud, String realId) {
        return generate(Instance.class, String.format("%s-%s", cloud.getId(), realId));
    }
    
    public String getImageId(Cloud cloud, String realId) {
        return generate(Image.class, String.format("%s-%s", cloud.getId(), realId));
    }

    

}
