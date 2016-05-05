package org.meridor.perspective.framework.storage.impl;

public final class StorageKey {

    public static String projectsById() {
        return "projects_by_id";
    }

    public static String instancesById() {
        return "instances_by_id";
    }
    
    public static String imagesById() {
        return "images_by_id";
    }

    public static String deletedInstancesByCloud() {
        return "deleted-instances";
    }
    
    public static String deletedImagesByCloud() {
        return "deleted-images";
    }
    
    public static String indexes() {
        return "indexes";
    }


    private StorageKey() {
    }
}
