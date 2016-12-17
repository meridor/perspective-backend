package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.pojo.*;

import java.util.List;
import java.util.Optional;

public interface Api {
    
    // Account operations
    
    Account getAccountInfo() throws Exception;
    
    // Project operations
    
    List<Region> listRegions() throws Exception;
    
    List<Size> listSizes() throws Exception;
    
    List<Key> listKeys() throws Exception;

    // Instance operations
    
    List<Droplet> listDroplets() throws Exception;
    
    Optional<Droplet> getDropletById(Integer dropletId) throws Exception;
    
    Integer addDroplet(Droplet droplet) throws Exception;
    
    String addAddress(Integer dropletId) throws Exception;
    
    void addAddress(Integer dropletId, String address) throws Exception;

    void deleteDroplet(Integer dropletId) throws Exception;

    void startDroplet(Integer dropletId) throws Exception;

    void shutdownDroplet(Integer dropletId) throws Exception;
    
    void powerOffDroplet(Integer dropletId) throws Exception;

    void rebootDroplet(Integer dropletId) throws Exception;

    void hardRebootDroplet(Integer dropletId) throws Exception;

    void renameDroplet(Integer dropletId, String newName) throws Exception;
    
    void resizeDroplet(Integer dropletId, String flavorId) throws Exception;

    void rebuildDroplet(Integer dropletId, Integer imageId) throws Exception;

    // Image operations
    
    List<Image> listImages() throws Exception;

    Optional<Image> getImageById(Integer imageId) throws Exception;

    void addImage(Integer dropletId, String imageName) throws Exception;
    
    void deleteImage(Integer imageId) throws Exception;
}
