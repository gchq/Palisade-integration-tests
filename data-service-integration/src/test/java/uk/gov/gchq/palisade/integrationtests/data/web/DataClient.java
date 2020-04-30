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

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import uk.gov.gchq.palisade.service.data.request.AddSerialiserRequest;
import uk.gov.gchq.palisade.service.data.request.ReadRequest;

@FeignClient(name = "data-service", url = "${web.client.data-service}")
public interface DataClient {

    @PostMapping(value = "/read/chunked", consumes = "application/json", produces = "application/octet-stream")
    Response readChunked(@RequestBody final ReadRequest request);

    @PostMapping(value = "/addSerialiser", consumes = "application/json", produces = "application/json")
    Boolean addSerialiser(@RequestBody final AddSerialiserRequest request);

    @GetMapping(path = "/actuator/health", produces = "application/json")
    Response getHealth();
}
