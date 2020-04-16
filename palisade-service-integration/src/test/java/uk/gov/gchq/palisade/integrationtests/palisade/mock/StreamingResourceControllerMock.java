package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.data.serialise.LineSerialiser;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.integrationtests.palisade.request.AddResourceRequest;
import uk.gov.gchq.palisade.integrationtests.palisade.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.integrationtests.palisade.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.integrationtests.palisade.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.integrationtests.palisade.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/resource-service-mock")
public class StreamingResourceControllerMock {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingResourceControllerMock.class);

    private final Serialiser<LeafResource> serialiser = new LineSerialiser<>() {
        @Override
        public String serialiseLine(final LeafResource obj) {
            try {
                return new ObjectMapper().writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                LOGGER.error("Encountered JSONProccessingException while serialising object {}", obj);
                LOGGER.error("Exception was ", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public LeafResource deserialiseLine(final String line) {
            LOGGER.warn("No implementation of deserialiseLine, ignoring argument {}", line);
            return null;
        }
    };

    public static Stream<LeafResource> getResources() {
        ConnectionDetail connectionDetail = new SimpleConnectionDetail().uri("data-service-mock");
        LeafResource resource = new FileResource()
                .id("root/resource-id")
                .type("type")
                .serialisedFormat("format")
                .connectionDetail(connectionDetail)
                .parent(new SystemResource().id("root"));
        return Stream.of(resource);
    }

    private void streamResources(OutputStream outputStream) throws IOException {
         serialiser.serialise(getResources(), outputStream);
    }

    @PostMapping(path = "/getResourcesById", consumes = "application/json", produces = "application/octet-stream")
    public ResponseEntity<StreamingResponseBody> getResourcesById(@RequestBody final GetResourcesByIdRequest request) {
        LOGGER.info("Invoking MOCKED getResourcesById");
        StreamingResponseBody stream = this::streamResources;
        return new ResponseEntity<>(stream, HttpStatus.OK);
    }

    @PostMapping(path = "/getResourcesByResource", consumes = "application/json", produces = "application/octet-stream")
    public ResponseEntity<StreamingResponseBody> getResourcesByResource(@RequestBody final GetResourcesByResourceRequest request) {
        LOGGER.info("Invoking MOCKED getResourcesByResource");
        StreamingResponseBody stream = this::streamResources;
        return new ResponseEntity<>(stream, HttpStatus.OK);
    }

    @PostMapping(path = "/getResourcesByType", consumes = "application/json", produces = "application/octet-stream")
    public ResponseEntity<StreamingResponseBody> getResourcesByType(@RequestBody final GetResourcesByTypeRequest request) {
        LOGGER.info("Invoking MOCKED getResourcesByType");
        StreamingResponseBody stream = this::streamResources;
        return new ResponseEntity<>(stream, HttpStatus.OK);
    }

    @PostMapping(path = "/getResourcesBySerialisedFormat", consumes = "application/json", produces = "application/octet-stream")
    public ResponseEntity<StreamingResponseBody> getResourcesBySerialisedFormat(@RequestBody final GetResourcesBySerialisedFormatRequest request) {
        LOGGER.info("Invoking MOCKED getResourcesBySerialisedFormat");
        StreamingResponseBody stream = this::streamResources;
        return new ResponseEntity<>(stream, HttpStatus.OK);
    }

    @PostMapping(path = "/addResource", consumes = "application/json", produces = "application/json")
    public Boolean addResource(@RequestBody final AddResourceRequest request) {
        LOGGER.info("Invoking MOCKED addResource");
        return true;
    }

}
