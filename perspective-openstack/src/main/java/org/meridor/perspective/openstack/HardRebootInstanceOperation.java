package org.meridor.perspective.openstack;

import org.jclouds.openstack.nova.v2_0.domain.RebootType;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

@Component
public class HardRebootInstanceOperation extends RebootInstanceOperation {
    
    @Override
    protected RebootType getRebootType() {
        return RebootType.HARD;
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to hard reboot instance";
    }

    @Override
    protected String getSuccessMessage() {
        return "Hard rebooted instance {} ({})";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{OperationType.HARD_REBOOT_INSTANCE};
    }
}
