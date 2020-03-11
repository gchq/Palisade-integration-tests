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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.request.CanAccessRequest;
import uk.gov.gchq.palisade.service.policy.request.CanAccessResponse;
import uk.gov.gchq.palisade.service.policy.request.GetPolicyRequest;
import uk.gov.gchq.palisade.service.policy.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.service.policy.service.PolicyService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@EnableFeignClients
@SpringBootTest(classes = PolicyApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class PolicyComponentTest extends PolicyTestCommon {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyCachingProxyTest.class);

    @Autowired
    Map<String, PolicyService> serviceMap;

    @Autowired
    PolicyClient policyClient;

    @Test
    public void contextLoads() {
        assertNotNull(serviceMap);
        assertNotEquals(serviceMap, Collections.emptyMap());
    }

    @Test
    public void isUp() {
        Response health = policyClient.getActuatorHealth();

        assertThat(health.status(), equalTo(200));
    }

    @Ignore // TODO: This test is failing with a 400 error (cannot use PassThroughRule as a subclass of Rule)
    @Test
    public void componentTest() {
        // Given there are resources and policies to be added
        Collection<LeafResource> resources = Collections.singleton(newFile);

        // When a resource is added
        SetResourcePolicyRequest addRequest = new SetResourcePolicyRequest().resource(newFile).policy(passThroughPolicy);
        addRequest.originalRequestId(new RequestId().id("test-id"));
        policyClient.setResourcePolicyAsync(addRequest);

        // Given it is accessible
        CanAccessRequest accessRequest = new CanAccessRequest().user(user).resources(resources).context(context);
        accessRequest.originalRequestId(new RequestId().id("test-id"));
        CanAccessResponse accessResponse = policyClient.canAccess(accessRequest);
        for (LeafResource resource: resources) {
            assertThat(accessResponse.getCanAccessResources(), hasItem(resource));
        }

        // When the policies on the resource are requested
        GetPolicyRequest getRequest = new GetPolicyRequest().user(user).resources(resources).context(context);
        getRequest.originalRequestId(new RequestId().id("test-id"));
        Map<LeafResource, Rules> getResponse = policyClient.getPolicySync(getRequest);
        LOGGER.info("Response: {}", getResponse);

        // Then the policy just added is found on the resource
        assertThat(getResponse.get(newFile), equalTo(passThroughPolicy.getRecordRules()));
    }
}
