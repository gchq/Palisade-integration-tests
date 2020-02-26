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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.request.CanAccessRequest;
import uk.gov.gchq.palisade.service.policy.request.CanAccessResponse;
import uk.gov.gchq.palisade.service.policy.request.GetPolicyRequest;
import uk.gov.gchq.palisade.service.policy.request.GetPolicyResponse;
import uk.gov.gchq.palisade.service.policy.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.service.policy.service.PolicyService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PolicyComponentTest extends PolicyTestCommon {
    @Autowired
    Map<String, PolicyService> serviceMap;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void contextLoads() {
        assertNotNull(serviceMap);
        assertNotEquals(serviceMap, Collections.emptyMap());
        assertNotNull(objectMapper);
    }

    @Test
    public void isUp() {
        final String health = restTemplate.getForObject("/actuator/health", String.class);

        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void componentTest() {
        // Given there are resources and policies to be added
        Collection<LeafResource> resources = Collections.singleton(newFile);

        // When a resource is added
        SetResourcePolicyRequest addRequest = new SetResourcePolicyRequest().resource(newFile).policy(passThroughPolicy);
        addRequest.originalRequestId(new RequestId().id("test-id"));
        restTemplate.put("/setResourcePolicyAsync", addRequest);

        // Given it is accessible
        CanAccessRequest accessRequest = new CanAccessRequest().user(user).resources(resources).context(context);
        accessRequest.originalRequestId(new RequestId().id("test-id"));
        CanAccessResponse accessResponse = restTemplate.postForObject("/canAccess", accessRequest, CanAccessResponse.class);
        for (LeafResource resource: resources) {
            assertThat(accessResponse.getCanAccessResources(), hasItem(resource));
        }

        // When the policies on the resource are requested
        GetPolicyRequest getRequest = new GetPolicyRequest().user(user).resources(resources).context(context);
        getRequest.originalRequestId(new RequestId().id("test-id"));
        GetPolicyResponse getResponse = restTemplate.postForObject("/getPolicySync", getRequest, GetPolicyResponse.class);

        // Then the policy just added is found on the resource
        assertThat(getResponse.getRecordRules().get(newFile), equalTo(passThroughPolicy.getRecordRules()));
    }
}
