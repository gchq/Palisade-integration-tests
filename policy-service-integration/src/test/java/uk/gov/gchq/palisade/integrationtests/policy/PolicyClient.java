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
package uk.gov.gchq.palisade.integrationtests.policy;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.policy.config.ApplicationConfiguration;
import uk.gov.gchq.palisade.service.policy.request.CanAccessRequest;
import uk.gov.gchq.palisade.service.policy.request.CanAccessResponse;
import uk.gov.gchq.palisade.service.policy.request.GetPolicyRequest;
import uk.gov.gchq.palisade.service.policy.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.service.policy.request.SetTypePolicyRequest;

import java.util.Map;

@FeignClient(name = "policy-service", url = "localhost:${server.port}", configuration = ApplicationConfiguration.class)
public interface PolicyClient {
    @GetMapping(value = "/actuator/health", produces = "application/json")
    Response getActuatorHealth();

    @PostMapping(value = "/canAccess", consumes = "application/json", produces = "application/json")
    CanAccessResponse canAccess(final CanAccessRequest request);

    @PostMapping(path = "/getPolicySync", consumes = "application/json", produces = "application/json")
    Map<LeafResource, Rules> getPolicySync(final GetPolicyRequest request);

    @PutMapping(value = "/setResourcePolicyAsync", consumes = "application/json", produces = "application/json")
    void setResourcePolicyAsync(final SetResourcePolicyRequest request);

    @PutMapping(value = "/setTypePolicyAsync", consumes = "application/json", produces = "application/json")
    void setTypePolicyAsync(final SetTypePolicyRequest request);
}
