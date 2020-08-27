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

package uk.gov.gchq.palisade.integrationtests.resource.web;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import uk.gov.gchq.palisade.service.resource.request.AddResourceRequest;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.service.resource.request.GetResourcesByTypeRequest;

@FeignClient(name = "resource-service", url = "${web.client.resource-service}")
public interface ResourceClient {

    @PostMapping(path = "/getResourcesById", consumes = "application/json", produces = "application/octet-stream")
    Response getResourcesById(@RequestBody final GetResourcesByIdRequest request);

    @PostMapping(path = "/getResourcesByResource", consumes = "application/json", produces = "application/octet-stream")
    Response getResourcesByResource(@RequestBody final GetResourcesByResourceRequest request);

    @PostMapping(path = "/getResourcesByType", consumes = "application/json", produces = "application/octet-stream")
    Response getResourcesByType(@RequestBody final GetResourcesByTypeRequest request);

    @PostMapping(path = "/getResourcesBySerialisedFormat", consumes = "application/json", produces = "application/octet-stream")
    Response getResourcesBySerialisedFormat(@RequestBody final GetResourcesBySerialisedFormatRequest request);

    @PostMapping(path = "/addResource", consumes = "application/json", produces = "application/json")
    Boolean addResource(@RequestBody final AddResourceRequest request);

    @GetMapping(path = "/actuator/health", produces = "application/json")
    Response getHealth();

}
