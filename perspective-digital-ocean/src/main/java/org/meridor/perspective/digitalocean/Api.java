package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Size;

import java.util.List;
import java.util.Optional;

public interface Api {
    
    // Project operations
    
    List<Region> listRegions() throws Exception;
    
    List<Size> listSizes() throws Exception;
    
    // Instance operations
    
    List<Droplet> listDroplets() throws Exception;
    
    Optional<Droplet> getDropletById(Integer dropletId) throws Exception;
    
    Integer addDroplet(Droplet droplet) throws Exception;

    void deleteDroplet(Integer dropletId) throws Exception;

    void startDroplet(Integer dropletId) throws Exception;

    void shutdownDroplet(Integer dropletId) throws Exception;

    void rebootDroplet(Integer dropletId) throws Exception;

    void hardRebootDroplet(Integer dropletId) throws Exception;

    void resizeDroplet(Integer dropletId, String flavorId) throws Exception;

    void rebuildDroplet(Integer dropletId, Integer imageId) throws Exception;

    // Image operations
    
    List<Image> listImages() throws Exception;
    
    Integer addImage(Integer dropletId, String imageName) throws Exception;
    
    void deleteImage(Integer imageId) throws Exception;
}
