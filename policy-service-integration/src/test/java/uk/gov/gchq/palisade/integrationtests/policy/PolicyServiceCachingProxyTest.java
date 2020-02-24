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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.IsTextResourceRule;
import uk.gov.gchq.palisade.policy.PassThroughRule;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.PredicateRule;
import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.request.Policy;
import uk.gov.gchq.palisade.service.policy.service.PolicyService;
import uk.gov.gchq.palisade.service.policy.service.PolicyServiceCachingProxy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;


// When registering data the Audit service must return 200 STATUS else test fails and return STATUS
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PolicyServiceCachingProxyTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyServiceCachingProxyTest.class);

    private static final User user = new User().userId("testUser");
    private static final User secretUser = new User().userId("secretTestUser").addAuths(new HashSet<>(Arrays.asList("Sensitive", "Secret")));
    private static final Context context = new Context().purpose("Testing");

    /**
     * Setup a collection of resources with policies like so:
     * /txt - only txt type files are viewable
     *   /txt/json - only json format files are viewable
     *     /txt/json/json.txt - an accessible json txt file
     *     /txt/json/json.avro - an inaccessible json avro file (breaks /txt rule)
     *     /txt/json/pickled.txt - an inaccessible pickle txt file (breaks /txt/json rule)
     *   /txt/sensitive - only users with sensitive auth can view
     *     /txt/sensitive/report.txt - an accessible (to sensitive auths) txt file
     *     /txt/sensitive/salary.csv - an inaccessible csv file (breaks /txt rule)
     *   /txt/secret - only users with secret auth can view, a purpose of testing will redact all record-level info
     *     /txt/secret/secrets.txt - an accessible (to secret auths) txt file
     * /new - a directory to be added with a pass-thru policy (do nothing)
     *   /new/file.exe - an accessible executable (not under /txt policy)
     **/

    // A system that only allows text files to be seen
    private static final SystemResource txtSystem = new SystemResource().id("/txt");
    private static final Policy txtPolicy = new Policy<>()
            .owner(user)
            .resourceLevelRule("Resource serialised format is txt", new IsTextResourceRule());

    // A directory that only allows JSON types
    private static final DirectoryResource jsonDirectory = new DirectoryResource().id("/txt/json").parent(txtSystem);
    private static final Policy jsonPolicy = new Policy<>()
            .owner(user)
            .resourceLevelRule("Resource type is json", (PredicateRule<Resource>) (resource, user, context) -> resource instanceof LeafResource && ((LeafResource) resource).getType().equals("json"));

    // A text file containing json data - this should be accessible
    private static final FileResource accessibleJsonTxtFile = new FileResource().id("/txt/json/json.txt").serialisedFormat("txt").type("json").parent(jsonDirectory);

    // A secret directory that allows only secret authorised users
    private static final DirectoryResource secretDirectory = new DirectoryResource().id("/txt/secret").parent(txtSystem);
    private static final Policy secretPolicy = new Policy<>()
            .owner(secretUser)
            .resourceLevelRule("Check user has 'Secret' auth", (PredicateRule<Resource>) (resource, user, context) -> user.getAuths().contains("Secret"))
            .recordLevelPredicateRule("Redact all with 'Testing' purpose", (record, user, context) -> context.getPurpose().equals("Testing"));

    // A secret file - accessible only to the secret user
    private static final FileResource secretTxtFile = new FileResource().id("/txt/secret/secrets.txt").serialisedFormat("txt").type("txt").parent(secretDirectory);

    private static final FileResource newFile = new FileResource().id("/new/file.exe").serialisedFormat("exe").type("elf").parent(new SystemResource().id("/new"));

    // A do-nothing policy to apply to leaf resources
    private static final Policy passThroughPolicy = new Policy<>()
            .owner(user)
            .resourceLevelRule("Does nothing", new PassThroughRule<>())
            .recordLevelRule("Does nothing", new PassThroughRule<>());

    private static final Set<DirectoryResource> directoryResources = new HashSet<>(Arrays.asList(jsonDirectory, secretDirectory));
    private static final Set<FileResource> fileResources = new HashSet<>(Arrays.asList(accessibleJsonTxtFile, secretTxtFile));

    @Autowired
    private PolicyServiceCachingProxy cacheProxy;
    @Autowired
    @Qualifier("impl")
    private PolicyService policyService;

    @Autowired
    private TestRestTemplate restTemplate;

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
    public void isUp() {
        final String health = restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
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
        Optional<Policy> policy = cacheProxy.getPolicy(newFile);

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
