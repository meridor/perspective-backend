package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.common.DropletStatus;
import com.myjeeva.digitalocean.pojo.Droplet;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.SHUTDOWN_INSTANCE;

@Component
public class ShutdownInstanceOperation extends BaseInstanceOperation {

    @Value("${perspective.digitalocean.shutdown.timeout:60000}")
    private Integer instanceShutdownTimeout;
    
    private static final Integer CHECK_DELAY = 5000;
    
    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> {
            try {
                Integer dropletId = Integer.valueOf(instance.getRealId());
                api.shutdownDroplet(dropletId);
                if (!waitForGracefulShutdown(api, dropletId)){
                    api.powerOffDroplet(dropletId);
                }
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private boolean waitForGracefulShutdown(Api api, Integer dropletId) throws Exception {
        int totalWaitingMs = 0;
        while (totalWaitingMs < instanceShutdownTimeout) {
            Optional<Droplet> dropletCandidate = api.getDropletById(dropletId);
            if (!dropletCandidate.isPresent()) {
                break;
            }
            if (dropletCandidate.get().getStatus() == DropletStatus.OFF) {
                return true;
            }
            totalWaitingMs += CHECK_DELAY;
            Thread.sleep(CHECK_DELAY);
        }
        return false;
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Shut down instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to shut down instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{SHUTDOWN_INSTANCE};
    }
    
}
