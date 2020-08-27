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

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.gchq.palisade.policy.PassThroughRule;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.service.PolicyServiceCachingProxy;
import uk.gov.gchq.palisade.service.request.Policy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("caffeine")
@SpringBootTest(classes = PolicyApplication.class, webEnvironment = WebEnvironment.NONE)
public class CaffeinePolicyCachingTest extends PolicyTestCommon {

    @Autowired
    private PolicyServiceCachingProxy policyService;

    @Autowired
    private CacheManager cacheManager;

    private void forceCleanUp() {
        List<String> caches = Arrays.asList("resourcePolicy", "typePolicy", "accessPolicy");
        caches.forEach(x -> ((Cache<?, ?>) Objects.requireNonNull(cacheManager.getCache(x)).getNativeCache()).cleanUp());
    }

    @BeforeEach
    public void setup() {
        // Add the system resource to the policy service
        assertThat(policyService.setResourcePolicy(TXT_SYSTEM, TXT_POLICY)).isEqualTo(TXT_POLICY);

        // Add the directory resources to the policy service
        assertThat(policyService.setResourcePolicy(JSON_DIRECTORY, JSON_POLICY)).isEqualTo(JSON_POLICY);
        assertThat(policyService.setResourcePolicy(SECRET_DIRECTORY, SECRET_POLICY)).isEqualTo(SECRET_POLICY);

        // Add the file resources to the policy service
        for (FileResource fileResource : FILE_RESOURCES) {
            assertThat(policyService.setResourcePolicy(fileResource, PASS_THROUGH_POLICY)).isEqualTo(PASS_THROUGH_POLICY);
        }
    }

    @Test
    public void testContextLoads() {
        assertThat(policyService).isNotNull();
    }

    @Test
    public void testAddedPolicyIsRetrievable() {
        // Given - resources have been added as above
        // Given there is no underlying policy storage (gets must be wholly cache-based)

        for (Resource resource : FILE_RESOURCES) {
            // When
            Optional<Policy> policy = policyService.getPolicy(resource);

            // Then
            assertThat(policy).isNotNull();
        }
    }

    @Test
    public void testNonExistentPolicyRetrieveFails() {
        // Given - the requested resource is not added

        // When
        Optional<Policy> policy = policyService.getPolicy(new FileResource().id("does not exist").type("null").serialisedFormat("null").parent(new SystemResource().id("also does not exist")));

        // Then
        assertThat(policy).isEmpty();
    }

    @Test
    public void testCacheMaxSize() {
        /// Given - the cache is overfilled
        Function<Integer, Resource> makeResource = i -> new StubResource(i.toString(), i.toString(), i.toString(), new SimpleConnectionDetail().serviceName(i.toString()));
        Function<Integer, Policy> makePolicy = i -> new Policy<>().resourceLevelRule(i.toString(), new PassThroughRule<>());
        for (int count = 0; count <= 100; ++count) {
            policyService.setResourcePolicy(makeResource.apply(count), makePolicy.apply(count));
        }

        // When - we try to get the first (now-evicted) entry
        forceCleanUp();
        Optional<Policy> cachedPolicy = policyService.getPolicy(makeResource.apply(0));

        // Then - it has been evicted
        assertThat(cachedPolicy).isEmpty();
    }

    @Test
    public void testCacheTtl() throws InterruptedException {
        // Given - the requested resource has policies available
        assertThat(policyService.getPolicy(ACCESSIBLE_JSON_TXT_FILE)).isPresent();
        // Given - a sufficient amount of time has passed

        Thread.sleep(2500);
        forceCleanUp();

        // When - an old entry is requested
        Optional<Policy> cachedPolicy = policyService.getPolicy(ACCESSIBLE_JSON_TXT_FILE);

        // Then - it has been evicted
        assertThat(cachedPolicy).isEmpty();
    }
}
