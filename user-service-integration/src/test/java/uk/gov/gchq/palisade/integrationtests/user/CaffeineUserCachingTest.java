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

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.user.UserApplication;
import uk.gov.gchq.palisade.service.user.exception.NoSuchUserIdException;
import uk.gov.gchq.palisade.service.user.service.UserServiceProxy;

import java.util.Collections;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ActiveProfiles("caffeine")
@SpringBootTest(classes = UserApplication.class, webEnvironment = WebEnvironment.NONE)
public class CaffeineUserCachingTest {

    @Autowired
    private UserServiceProxy userService;

    @Autowired
    private CacheManager cacheManager;

    private void forceCleanUp() {
        ((Cache<?, ?>) cacheManager.getCache("users").getNativeCache()).cleanUp();
    }

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
    public void maxSizeTest() {
        // Given - many users are added and cached (cache size set to 100 in application.yaml)
        Function<Integer, User> makeUser = i -> new User().userId(new UserId().id("max-size-" + i.toString() + "-test-user"));
        for (int count = 0; count <= 150; ++count) {
            userService.addUser(makeUser.apply(count));
        }

        // When - we try to get the first (now-evicted) user to be added
        forceCleanUp();
        User notFound = userService.getUser(makeUser.apply(0).getUserId());

        // Then - it is no longer found, it has been evicted
        // ie. throw NoSuchUserIdException
        fail("Got user \"" + notFound.toString() + "\" from cache, but it should have been evicted");
    }

    @Test(expected = NoSuchUserIdException.class)
    public void ttlTest() throws InterruptedException {
        // Given - a user was added a long time ago (ttl set to 1s in application.yaml)
        User user = new User().userId("ttl-test-user").addAuths(Collections.singleton("authorisation")).addRoles(Collections.singleton("role"));
        userService.addUser(user);

        Thread.sleep(1000);

        // When - we try to access stale cache data
        forceCleanUp();
        User notFound = userService.getUser(user.getUserId());

        // Then - it is no longer found, it has been evicted
        // ie. throw NoSuchUserIdException
        fail("Got user \"" + notFound.toString() + "\" from cache, but it should have been evicted");
    }
}
