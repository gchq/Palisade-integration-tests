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

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.IsTextResourceRule;
import uk.gov.gchq.palisade.policy.PassThroughRule;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.PredicateRule;
import uk.gov.gchq.palisade.service.request.Policy;
//import uk.gov.gchq.palisade.service.policy.request.Policy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PolicyTestCommon {
    static final User USER = new User().userId("testUser");
    static final User SECRET_USER = new User().userId("secretTestUser").addAuths(new HashSet<>(Arrays.asList("Sensitive", "Secret")));
    static final Context CONTEXT = new Context().purpose("Testing");

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
    static final SystemResource TXT_SYSTEM = new SystemResource().id("/txt");
    static final Policy TXT_POLICY = new Policy<>()
            .owner(USER)
            .resourceLevelRule("Resource serialised format is txt", new IsTextResourceRule());

    // A directory that only allows JSON types
    static final DirectoryResource JSON_DIRECTORY = new DirectoryResource().id("/txt/json").parent(TXT_SYSTEM);
    static final Policy JSON_POLICY = new Policy<>()
            .owner(USER)
            .resourceLevelRule("Resource type is json", (PredicateRule<Resource>) (resource, user, context) -> resource instanceof LeafResource && ((LeafResource) resource).getType().equals("json"));

    // A text file containing json data - this should be accessible
    static final FileResource ACCESSIBLE_JSON_TXT_FILE = new FileResource().id("/txt/json/json.txt").serialisedFormat("txt").type("json").parent(JSON_DIRECTORY);

    // A secret directory that allows only secret authorised users
    static final DirectoryResource SECRET_DIRECTORY = new DirectoryResource().id("/txt/secret").parent(TXT_SYSTEM);
    static final Policy SECRET_POLICY = new Policy<>()
            .owner(SECRET_USER)
            .resourceLevelRule("Check user has 'Secret' auth", (PredicateRule<Resource>) (resource, user, context) -> user.getAuths().contains("Secret"))
            .recordLevelPredicateRule("Redact all with 'Testing' purpose", (record, user, context) -> !context.getPurpose().equals("Testing"));

    // A secret file - accessible only to the secret user
    static final FileResource SECRET_TXT_FILE = new FileResource().id("/txt/secret/secrets.txt").serialisedFormat("txt").type("txt").parent(SECRET_DIRECTORY);

    static final FileResource NEW_FILE = new FileResource().id("/new/file.exe").serialisedFormat("exe").type("elf").parent(new SystemResource().id("/new"));

    // A do-nothing policy to apply to leaf resources
    static final Policy PASS_THROUGH_POLICY = new Policy<>()
            .owner(USER)
            .resourceLevelRule("Does nothing", new PassThroughRule<>())
            .recordLevelRule("Does nothing", new PassThroughRule<>());

    static final Set<DirectoryResource> DIRECTORY_RESOURCES = new HashSet<>(Arrays.asList(JSON_DIRECTORY, SECRET_DIRECTORY));
    static final Set<FileResource> FILE_RESOURCES = new HashSet<>(Arrays.asList(ACCESSIBLE_JSON_TXT_FILE, SECRET_TXT_FILE));
}
