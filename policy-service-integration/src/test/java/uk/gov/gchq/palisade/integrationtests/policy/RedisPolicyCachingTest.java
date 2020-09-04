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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.gchq.palisade.integrationtests.policy.config.PolicyTestConfiguration;
import uk.gov.gchq.palisade.integrationtests.policy.config.RedisTestConfiguration;
import uk.gov.gchq.palisade.policy.IsTextResourceRule;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.service.PolicyService;
import uk.gov.gchq.palisade.service.policy.service.PolicyServiceCachingProxy;
import uk.gov.gchq.palisade.service.request.Policy;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(PolicyTestConfiguration.class)
@ActiveProfiles("redis")
@SpringBootTest(classes = {PolicyApplication.class, RedisTestConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@ComponentScan(basePackages = "uk.gov.gchq.palisade")
public class RedisPolicyCachingTest extends PolicyTestCommon {

    @Autowired
    private PolicyServiceCachingProxy cacheProxy;

    @Autowired
    @Qualifier("impl")
    private PolicyService policyService;

    @BeforeEach
    public void setup() {
        // Add the system resource to the policy service
        assertThat(cacheProxy.setResourcePolicy(TXT_SYSTEM, TXT_POLICY)).isEqualTo(TXT_POLICY);

        // Add the directory resources to the policy service
        assertThat(cacheProxy.setResourcePolicy(JSON_DIRECTORY, JSON_POLICY)).isEqualTo(JSON_POLICY);
        assertThat(cacheProxy.setResourcePolicy(SECRET_DIRECTORY, SECRET_POLICY)).isEqualTo(SECRET_POLICY);

        // Add the file resources to the policy service
        for (FileResource fileResource : FILE_RESOURCES) {
            assertThat(cacheProxy.setResourcePolicy(fileResource, PASS_THROUGH_POLICY)).isEqualTo(PASS_THROUGH_POLICY);
        }
    }

    @Test
    public void testContextLoads() {
        assertThat(policyService).isNotNull();
        assertThat(cacheProxy).isNotNull();
    }

    @Test
    public void testAddedPolicyIsRetrievable() {
        // Given - resources have been added as above
        // Given there is no underlying policy storage (gets must be wholly cache-based)

        for (Resource resource : FILE_RESOURCES) {
            // When
            Policy policy = cacheProxy.getPolicy(resource).get();

            // Then
            assertThat(policy).isNotNull();
        }
    }

    @Test
    public void testNonExistentPolicyRetrieveFails() {
        // Given - the requested resource is not added

        // When
        Optional<Policy> policy = cacheProxy.getPolicy(new FileResource().id("does not exist").type("null").serialisedFormat("null").parent(new SystemResource().id("also does not exist")));

        // Then
        assertThat(policy).isEmpty();
    }

    @Test
    public void testUpdatePolicy() {
        // Given I add a policy and resource
        final SystemResource systemResource = new SystemResource().id("/txt");
        final Policy policy = new Policy<>()
                .owner(USER)
                .resourceLevelRule("Resource serialised format is txt", new IsTextResourceRule());
        cacheProxy.setResourcePolicy(systemResource, policy);

        //Then I update the Policies resourceLevelRules
        final Policy newPolicy = new Policy<>()
                .owner(USER)
                .resourceLevelRule("NewSerialisedFormat", new IsTextResourceRule());
        cacheProxy.setResourcePolicy(systemResource, newPolicy);

        // When
        Optional<Policy> returnedPolicy = cacheProxy.getPolicy(systemResource);

        // Then the returned policy should have the updated resource rules
        assertThat(returnedPolicy).isPresent();
        assertThat(returnedPolicy.get().getResourceRules()).isEqualTo(newPolicy.getResourceRules());
    }

    @Test
    public void testCacheTtl() throws InterruptedException {
        // Given - the requested resource has policies available
        assertThat(cacheProxy.getPolicy(ACCESSIBLE_JSON_TXT_FILE)).isNotNull();

        // Given - a sufficient amount of time has passed
        Thread.sleep(2500);

        // When - an old entry is requested
        Optional<Policy> cachedPolicy = cacheProxy.getPolicy(ACCESSIBLE_JSON_TXT_FILE);

        // Then - it has been evicted
        assertThat(cachedPolicy).isEmpty();
    }
}
