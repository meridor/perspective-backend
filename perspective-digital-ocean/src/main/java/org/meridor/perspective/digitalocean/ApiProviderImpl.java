package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class ApiProviderImpl implements ApiProvider {

    private static final Integer PER_PAGE = 25;

    @Override
    public Api getApi(Cloud cloud) {
        return new ApiImpl(cloud);
    }

    @Override
    public void forEachRegion(Cloud cloud, BiConsumer<String, Api> action) throws Exception {
        Api api = getApi(cloud);
        api.listRegions().forEach(cr -> action.accept(cr.getName(), new ApiImpl(cloud)));

    }

    private class ApiImpl implements Api {

        private final DigitalOcean api;

        ApiImpl(Cloud cloud) {
            this.api = createApi(cloud);
        }

        private DigitalOcean createApi(Cloud cloud) {
            return new DigitalOceanClient(cloud.getCredential());
        }

        @Override
        public List<Region> listRegions() throws Exception {
            return listImpl(pn -> {
                try {
                    return api.getAvailableRegions(pn).getRegions();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public List<Size> listSizes() throws Exception {
            return listImpl(pn -> {
                try {
                    return api.getAvailableSizes(pn).getSizes();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public List<Key> listKeys() throws Exception {
            return listImpl(pn -> {
                try {
                    return api.getAvailableKeys(pn).getKeys();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        }

        @Override
        public List<Droplet> listDroplets() throws Exception {
            return listImpl(pn -> {
                try {
                    return api.getAvailableDroplets(pn, PER_PAGE).getDroplets();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public Optional<Droplet> getDropletById(Integer dropletId) throws Exception {
            return Optional.ofNullable(api.getDropletInfo(dropletId));
        }

        @Override
        public Integer addDroplet(Droplet droplet) throws Exception {
            return api.createDroplet(droplet).getId();
        }

        @Override
        public String addAddress(Integer dropletId) throws Exception {
            return api.createFloatingIP(dropletId).getIp();
        }

        @Override
        public void addAddress(Integer dropletId, String address) throws Exception {
            api.assignFloatingIP(dropletId, address);
        }

        @Override
        public void deleteDroplet(Integer dropletId) throws Exception {
            api.deleteDroplet(dropletId);
        }

        @Override
        public void startDroplet(Integer dropletId) throws Exception {
            //TODO: what is the right API method?
            api.powerOnDroplet(dropletId);
        }

        @Override
        public void shutdownDroplet(Integer dropletId) throws Exception {
            api.shutdownDroplet(dropletId);
        }

        @Override
        public void rebootDroplet(Integer dropletId) throws Exception {
            api.rebootDroplet(dropletId);
        }

        @Override
        public void hardRebootDroplet(Integer dropletId) throws Exception {
            api.powerCycleDroplet(dropletId); //TODO: is this correct?
        }

        @Override
        public void resizeDroplet(Integer dropletId, String flavorId) throws Exception {
            api.resizeDroplet(dropletId, flavorId);
        }

        @Override
        public void rebuildDroplet(Integer dropletId, Integer imageId) throws Exception {
            api.rebuildDroplet(dropletId, imageId);
        }

        @Override
        public List<Image> listImages() throws Exception {
            return listImpl(pn -> {
                try {
                    return api.getAvailableImages(pn, PER_PAGE).getImages();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public Optional<Image> getImageById(Integer imageId) throws Exception {
            return Optional.ofNullable(api.getImageInfo(imageId));
        }

        @Override
        public Integer addImage(Integer dropletId, String imageName) throws Exception {
            return api.takeDropletSnapshot(dropletId, imageName).getId();
        }

        @Override
        public void deleteImage(Integer imageId) throws Exception {
            api.deleteImage(imageId);
        }

        private <T> List<T> listImpl(Function<Integer, List<T>> action) {
            return listImpl(1, action, new ArrayList<>());
        }

        private <T> List<T> listImpl(int pageNo, Function<Integer, List<T>> action, List<T> alreadyFetchedEntities) {
            try {
                alreadyFetchedEntities.addAll(action.apply(pageNo));
                return listImpl(pageNo + 1, action, alreadyFetchedEntities);
            } catch (Exception e) {
                return alreadyFetchedEntities;
            }
        }
    }
}
