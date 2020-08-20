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
package uk.gov.gchq.palisade.integrationtests.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.integrationtests.user.config.RedisTestConfiguration;
import uk.gov.gchq.palisade.service.user.UserApplication;
import uk.gov.gchq.palisade.service.user.exception.NoSuchUserIdException;
import uk.gov.gchq.palisade.service.user.service.UserServiceProxy;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ActiveProfiles("redis")
@SpringBootTest(classes = {UserApplication.class, RedisTestConfiguration.class}, webEnvironment = WebEnvironment.NONE)
public class RedisUserCachingTest {

    @Autowired
    private UserServiceProxy userService;

    @Test
    public void addedUserIsRetrievable() {
        // Given
        User user = new User().userId("added-user").addAuths(Collections.singleton("authorisation")).addRoles(Collections.singleton("role"));

        // When
        User addedUser = userService.addUser(user);
        // Then
        assertThat(addedUser, equalTo(user));

        // When
        User getUser = userService.getUser(user.getUserId());
        // Then
        assertThat(getUser, equalTo(user));
    }

    @Test(expected = NoSuchUserIdException.class)
    public void nonExistentUserRetrieveFails() {
        // Given
        UserId userId = new UserId().id("definitely-not-a-real-user");

        // When
        userService.getUser(userId);
        // Then - throw
    }

    @Test
    public void updateUserTest() {
        // Given
        User user = new User().userId("updatable-user").addAuths(Collections.singleton("auth")).addRoles(Collections.singleton("role"));
        User update = new User().userId("updatable-user").addAuths(Collections.singleton("newAuth")).addRoles(Collections.singleton("newRole"));

        // When
        userService.addUser(user);
        userService.addUser(update);

        User updatedUser = userService.getUser(user.getUserId());

        // Then
        assertThat(updatedUser, equalTo(update));
    }

    @Test(expected = NoSuchUserIdException.class)
    public void ttlTest() {
        // Given - a user was added a long time ago (ttl set to 1s in application.yaml)
        User user = new User().userId("ttl-test-user").addAuths(Collections.singleton("authorisation")).addRoles(Collections.singleton("role"));
        userService.addUser(user);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - we try to access stale cache data
        User notFound = userService.getUser(user.getUserId());

        // Then - it is no longer found, it has been evicted
        // ie. throw NoSuchUserIdException
        fail("Got user \"" + notFound.toString() + "\" from cache, but it should have been evicted");
    }
}
