package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.pojo.*;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.meridor.perspective.digitalocean.DigitalOceanUtils.*;

@Component
public class TestApiProvider implements ApiProvider {

    @Override
    public Api getApi(Cloud cloud) {
        return new ApiImpl();
    }

    @Override
    public void forEachRegion(Cloud cloud, BiConsumer<String, Api> action) throws Exception {
        Api api = getApi(cloud);
        api.listRegions().forEach(cr -> action.accept(cr.getSlug(), new ApiImpl()));
    }

    private class ApiImpl implements Api {

        @Override
        public Account getAccountInfo() throws Exception {
            return getAccount();
        }

        @Override
        public List<Region> listRegions() throws Exception {
            return Arrays.asList(getRegion("one"), getRegion("two"));
        }

        @Override
        public List<Size> listSizes() throws Exception {
            return Collections.singletonList(getSize());
        }

        @Override
        public List<Key> listKeys() throws Exception {
            return Collections.singletonList(getKey());
        }

        @Override
        public List<Droplet> listDroplets() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Droplet> getDropletById(Integer dropletId) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer addDroplet(Droplet droplet) throws Exception {
            return getRandomInteger();
        }

        @Override
        public String addAddress(Integer dropletId) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAddress(Integer dropletId, String address) throws Exception {

        }

        @Override
        public void deleteDroplet(Integer dropletId) throws Exception {

        }

        @Override
        public void startDroplet(Integer dropletId) throws Exception {

        }

        @Override
        public void shutdownDroplet(Integer dropletId) throws Exception {

        }

        @Override
        public void powerOffDroplet(Integer dropletId) throws Exception {

        }

        @Override
        public void rebootDroplet(Integer dropletId) throws Exception {

        }

        @Override
        public void hardRebootDroplet(Integer dropletId) throws Exception {

        }

        @Override
        public void resizeDroplet(Integer dropletId, String flavorId) throws Exception {

        }

        @Override
        public void rebuildDroplet(Integer dropletId, Integer imageId) throws Exception {

        }

        @Override
        public List<Image> listImages() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Image> getImageById(Integer imageId) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer addImage(Integer dropletId, String imageName) throws Exception {
            return getRandomInteger();
        }

        @Override
        public void deleteImage(Integer imageId) throws Exception {

        }

    }

}
