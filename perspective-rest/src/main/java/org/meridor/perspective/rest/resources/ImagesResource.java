package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.events.ImageDeletingEvent;
import org.meridor.perspective.events.ImageSavingEvent;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.framework.storage.IllegalQueryException;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.meridor.perspective.beans.DestinationName.TASKS;
import static org.meridor.perspective.events.EventFactory.*;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;
import static org.meridor.perspective.rest.resources.ResponseUtils.clientError;
import static org.meridor.perspective.rest.resources.ResponseUtils.notFound;

@Component
@Path("/images")
public class ImagesResource {

    private static final Logger LOG = LoggerFactory.getLogger(ImagesResource.class);

    @Autowired
    private ImagesAware imagesAware;

    @Destination(TASKS)
    private Producer producer;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getImages(@QueryParam("query") String query) {
        try {
            LOG.info("Getting images list for query = {}", query);
            List<Image> images = new ArrayList<>(imagesAware.getImages(Optional.ofNullable(query)));
            return Response.ok(new GenericEntity<List<Image>>(images){}).build();
        } catch (IllegalQueryException e) {
            return clientError(String.format("Illegal query %s", query));
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{imageId}")
    public Response getImageById(@PathParam("imageId") String imageId) {
        LOG.info("Getting image for imageId = {}", imageId);
        Optional<Image> image = imagesAware.getImage(imageId);
        return image.isPresent() ?
                Response.ok(image.get()).build() :
                notFound(String.format("Image with id = %s not found", imageId));
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response saveImages(List<Image> images) {
        for (Image image : images) {
            LOG.info("Queuing image {} for saving", image);
            String temporaryId = uuid();
            image.setId(temporaryId);
            image.setCreated(now());
            image.setTimestamp(now());
            image.setState(ImageState.QUEUED);
            imagesAware.saveImage(image);
            ImageSavingEvent event = imageEvent(ImageSavingEvent.class, image);
            event.setTemporaryImageId(temporaryId);
            producer.produce(message(image.getCloudType(), event));
        }
        return Response.ok().build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteImages(List<Image> images) {
        for (Image image : images) {
            LOG.debug("Queuing image {} ({}) for removal", image.getName(), image.getId());
            image.setState(ImageState.DELETING);
            imagesAware.saveImage(image);
            ImageDeletingEvent event = imageEvent(ImageDeletingEvent.class, image);
            producer.produce(message(image.getCloudType(), event));
        }
        return Response.ok().build();
    }

}
