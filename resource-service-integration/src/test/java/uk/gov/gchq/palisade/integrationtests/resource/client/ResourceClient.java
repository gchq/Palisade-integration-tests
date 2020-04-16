/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.integrationtests.resource.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.serialise.LineSerialiser;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.resource.AbstractLeafResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesByTypeRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceClient implements Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceClient.class);
    private final ResourceFeignClient client;
    private final ObjectMapper objectMapper;

    private final Serialiser<LeafResource> serialiser = new LineSerialiser<>() {
        @Override
        public LeafResource deserialiseLine(final String line) {
            try {
                return objectMapper.readValue(line, AbstractLeafResource.class);
            } catch (JsonProcessingException e) {
                LOGGER.error("Encountered JSONProccessingException while deserialising line {}", line);
                LOGGER.error("Exception was ", e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                LOGGER.error("Encountered IOException while deserialising line {}", line);
                LOGGER.error("Exception was ", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public String serialiseLine(final LeafResource obj) {
            LOGGER.warn("No implementation of serialiseLine, ignoring argument {}", obj);
            return null;
        }
    };


    public ResourceClient(final ResourceFeignClient resourceFeignClient, final ObjectMapper objectMapper) {
        this.client = resourceFeignClient;
        this.objectMapper = objectMapper;
    }

    public Stream<LeafResource> getResourcesById(final GetResourcesByIdRequest request) {
        LOGGER.debug("Getting resources by id from resource service: {}", request);
        return getResourcesFromFeignResponse(() -> client.getResourcesById(request));
    }
    public Stream<LeafResource> getResourcesById(final String resourceId) {
        return getResourcesById(new GetResourcesByIdRequest().resourceId(resourceId));
    }

    public Stream<LeafResource> getResourcesByResource(final GetResourcesByResourceRequest request) {
        LOGGER.debug("Getting resources by resource from resource service: {}", request);
        return getResourcesFromFeignResponse(() -> client.getResourcesByResource(request));
    }
    public Stream<LeafResource> getResourcesByResource(final Resource resource) {
        return getResourcesByResource(new GetResourcesByResourceRequest().resource(resource));
    }

    public Stream<LeafResource> getResourcesByType(final GetResourcesByTypeRequest request) {
        LOGGER.debug("Getting resources by type from resource service: {}", request);
        return getResourcesFromFeignResponse(() -> client.getResourcesByType(request));
    }
    public Stream<LeafResource> getResourcesByType(final String type) {
        return getResourcesByType(new GetResourcesByTypeRequest().type(type));
    }

    public Stream<LeafResource> getResourcesBySerialisedFormat(final GetResourcesBySerialisedFormatRequest request) {
        LOGGER.debug("Getting resources by serialised format from resource service: {}", request);
        return getResourcesFromFeignResponse(() -> client.getResourcesBySerialisedFormat(request));
    }
    public Stream<LeafResource> getResourcesBySerialisedFormat(final String serialisedFormat) {
        return getResourcesBySerialisedFormat(new GetResourcesBySerialisedFormatRequest().serialisedFormat(serialisedFormat));
    }

    private Stream<LeafResource> getResourcesFromFeignResponse(final Supplier<Response> feignCall) {
        try {
            InputStream responseStream = feignCall.get().body().asInputStream();
            Stream<LeafResource> resourceStream = serialiser.deserialise(responseStream);
            LOGGER.info("Got resources: {}", resourceStream);
            return resourceStream;
        } catch (IOException e) {
            LOGGER.error("IOException getting response body input stream");
            LOGGER.error("Exception was ", e);
            throw new RuntimeException(e); //rethrow the exception
        }
    }

    public Response getHealth() {
        try {
            return this.client.getHealth();
        } catch (Exception ex) {
            LOGGER.error("Failed to get health: {}", ex.getMessage());
            throw new RuntimeException(ex); //rethrow the exception
        }
    }
}
