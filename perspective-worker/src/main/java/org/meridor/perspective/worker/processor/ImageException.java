package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Image;

public class ImageException extends RuntimeException {

    private final Image image;

    public ImageException(String message, Image image) {
        super(message);
        this.image = image;
    }

    public Image getImage() {
        return image;
    }
}
