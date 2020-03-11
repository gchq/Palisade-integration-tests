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

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.policy.PassThroughRule;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.request.Policy;
import uk.gov.gchq.palisade.service.policy.service.PolicyService;
import uk.gov.gchq.palisade.service.policy.service.PolicyServiceCachingProxy;

import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class PolicyCachingProxyTest extends PolicyTestCommon {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyCachingProxyTest.class);

    @Autowired
    private PolicyServiceCachingProxy cacheProxy;
    @Autowired
    @Qualifier("impl")
    private PolicyService policyService;

    @Before
    public void setup() {
        // Add the system resource to the policy service
        assertThat(cacheProxy.setResourcePolicy(txtSystem, txtPolicy), CoreMatchers.equalTo(txtPolicy));

        // Add the directory resources to the policy service
        assertThat(cacheProxy.setResourcePolicy(jsonDirectory, jsonPolicy), CoreMatchers.equalTo(jsonPolicy));
        assertThat(cacheProxy.setResourcePolicy(secretDirectory, secretPolicy), CoreMatchers.equalTo(secretPolicy));

        // Add the file resources to the policy service
        for (FileResource fileResource : fileResources) {
            assertThat(cacheProxy.setResourcePolicy(fileResource, passThroughPolicy), CoreMatchers.equalTo(passThroughPolicy));
        }
    }

    @Test
    public void contextLoads() {
        assertNotNull(policyService);
        assertNotNull(cacheProxy);
    }

    @Test
    public void addedPolicyIsRetrievable() {
        // Given - resources have been added as above
        // Given there is no underlying policy storage (gets must be wholly cache-based)

        for (Resource resource : fileResources) {
            // When
            Optional<Policy> policy = cacheProxy.getPolicy(resource);

            // Then
            assertTrue(policy.isPresent());
        }
    }

    @Test
    public void nonExistentPolicyRetrieveFails() {
        // Given - the requested resource is not added

        // When
        Optional<Policy> policy = cacheProxy.getPolicy(new FileResource().id("does not exist").type("null").serialisedFormat("null").parent(new SystemResource().id("also does not exist")));

        // Then
        assertTrue(policy.isEmpty());
    }

    @Test
    public void cacheMaxSizeTest() {
        /// Given - the cache is overfilled
        Function<Integer, Resource> makeResource = i -> new StubResource(i.toString(), i.toString(), i.toString());
        Function<Integer, Policy> makePolicy = i -> new Policy<>().resourceLevelRule(i.toString(), new PassThroughRule<>());
        for (int count = 0; count <= 100; ++count) {
            cacheProxy.setResourcePolicy(makeResource.apply(count), makePolicy.apply(count));
        }

        // When - an old entry is requested
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Optional<Policy> cachedPolicy = cacheProxy.getPolicy(makeResource.apply(0));

        // Then - it has been evicted
        assertTrue(cachedPolicy.isEmpty());
    }

    @Test
    public void cacheTtlTest() {
        // Given - the requested resource has policies available
        assumeTrue(cacheProxy.getPolicy(accessibleJsonTxtFile).isPresent());
        // Given - a sufficient amount of time has passed
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // When - an old entry is requested
        Optional<Policy> cachedPolicy = cacheProxy.getPolicy(accessibleJsonTxtFile);

        // Then - it has been evicted
        assertTrue(cachedPolicy.isEmpty());
    }
}
