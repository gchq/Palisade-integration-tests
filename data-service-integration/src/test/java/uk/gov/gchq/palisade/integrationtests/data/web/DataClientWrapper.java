/*
 * Copyright 2020 Crown Copyright
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

package uk.gov.gchq.palisade.integrationtests.data.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.data.serialise.Serialiser;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.reader.common.DataFlavour;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.data.request.AddSerialiserRequest;
import uk.gov.gchq.palisade.service.data.request.ReadRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DataClientWrapper implements Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataClientWrapper.class);
    private final DataClient client;
    private final ObjectMapper mapper;
    private final Serialiser<Employee> serialiser = new AvroSerialiser<>(Employee.class);

    public DataClientWrapper(final DataClient client, final ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    /**
     * Passes the request to the data-service to be processed
     *
     * @param request   a {@link ReadRequest} containing the token and a {@link LeafResource} to be read
     * @return          a {@link Stream} of {@link Employee} objects
     */
    public Stream<Employee> readChunked(final ReadRequest request) {
        LOGGER.debug("Reading request: {}", request);
        return getFromFeignResponse(() -> client.readChunked(request));
    }

    /**
     * Converts the returned data into a stream of objects
     *
     * @param feignCall     a {@link Supplier} containing the {@link Response} from feign
     * @return              a {@link Stream} of {@link Employee} objects
     */
    private Stream<Employee> getFromFeignResponse(final Supplier<Response> feignCall) {
        try {
            InputStream responseStream = feignCall.get().body().asInputStream();
            Stream<Employee> employeeStream = serialiser.deserialise(responseStream);
            LOGGER.info("Response: {}", employeeStream);
            return employeeStream;
        } catch (IOException ex) {
            LOGGER.error("Error encountered getting body Input Stream. Exception: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Add a serialiser to the data-service so that it can deserialise the data
     *
     * @param request   an {@link AddSerialiserRequest} that conatins the {@link Serialiser} and the {@link DataFlavour}
     * @return          a {@link Boolean} value
     */
    public Boolean addSerialiser(final AddSerialiserRequest request) {
        return client.addSerialiser(request);
    }

    /**
     * Get the health of the service
     *
     * @return      a feign {@link Response} containing the service health details
     */
    public Response getHealth() {
        try {
            return this.client.getHealth();
        } catch (Exception ex) {
            LOGGER.error("Failed to get health: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
