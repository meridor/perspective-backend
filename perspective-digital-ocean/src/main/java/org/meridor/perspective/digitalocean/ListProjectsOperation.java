package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.pojo.Account;
import com.myjeeva.digitalocean.pojo.Key;
import com.myjeeva.digitalocean.pojo.Size;
import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.beans.Quota;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.AbstractListProjectsOperation;
import org.meridor.perspective.worker.operation.OperationUtils;
import org.meridor.perspective.worker.operation.RegionsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListProjectsOperation extends AbstractListProjectsOperation<String, Api> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    private final ApiProvider apiProvider;

    private final OperationUtils operationUtils;

    @Autowired
    public ListProjectsOperation(ApiProvider apiProvider, OperationUtils operationUtils) {
        this.apiProvider = apiProvider;
        this.operationUtils = operationUtils;
    }

    @Override
    protected Project processProject(Cloud cloud, String region, Api api) {
        try {
            Project project = operationUtils.createProject(cloud, region);
            addFlavors(project, api);
            addKeyPairs(project, api);
            addQuota(project, api);
            return project;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected RegionsAware<String, Api> getRegionsAware() {
        return apiProvider;
    }

    private void addFlavors(Project project, Api api) throws Exception {
        for (Size size : api.listSizes()) {
            Flavor flavor = new Flavor();
            flavor.setId(size.getSlug());
            flavor.setVcpus(size.getVirutalCpuCount());
            flavor.setRam(size.getMemorySizeInMb());
            flavor.setRootDisk(size.getDiskSize());
            flavor.setEphemeralDisk(0);
            flavor.setHasSwap(false);
            flavor.setIsPublic(true);
            String flavorName = String.format(
                    "c%d-m%d-d%d-n%d-p%s",
                    size.getVirutalCpuCount(),
                    size.getMemorySizeInMb(),
                    size.getDiskSize(),
                    size.getTransfer(),
                    String.valueOf(size.getPriceMonthly())
            );
            flavor.setName(flavorName);
            project.getFlavors().add(flavor);
        }
    }
    
    private void addKeyPairs(Project project, Api api) throws Exception {
        for (Key key : api.listKeys()) {
            Keypair keypair = new Keypair();
            keypair.setName(key.getName());
            keypair.setPublicKey(key.getPublicKey());
            keypair.setFingerprint(key.getFingerprint());
            project.getKeypairs().add(keypair);
        }
    }
    
    private void addQuota(Project project, Api api) throws Exception {
        Account accountInfo = api.getAccountInfo();
        Quota quota = new Quota();
        if (accountInfo.getDropletLimit() != null) {
            quota.setInstances(String.valueOf(accountInfo.getDropletLimit()));
        }
        if (accountInfo.getFloatingIPLimit() != null) {
            quota.setIps(String.valueOf(accountInfo.getFloatingIPLimit()));
        }
        project.setQuota(quota);
    }

}
