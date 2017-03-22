package controllers.api;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import models.Organization;
import models.PublicUser;
import models.User;
import models.UserToPublicUserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import usecases.repositories.UserRepository;
import utils.WithFlowUpApplication;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

import play.mvc.Http.Session;

import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest extends WithFlowUpApplication {

    private static final Session session = new Session(new HashMap<>());

    @Mock
    private UserRepository userRepository;
    private PlayAuthenticate auth;

    @Override
    protected Application provideApplication() {
        auth = new GuiceApplicationBuilder().injector().instanceOf(PlayAuthenticate.class);
        auth = spy(auth);
        return new GuiceApplicationBuilder()
                .overrides(bind(PlayAuthenticate.class).toInstance(auth))
                .overrides(bind(UserRepository.class).toInstance(userRepository))
                .build();
    }

    @Test
    public void returnsUnauthorizedIfTheUserIsNotLoggedIn() {
        Result result = getUser();

        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void returnsPublicUserInfoIfTheUserIsLoggedInAndTheUserHasNoApps() {
        User user = givenAUser(false);

        authenticateUser(session, user);
        Result result = getUser(session);

        assertContainsPublicUserInfo(result, user);
    }

    @Test
    public void returnsPublicUserInfoIfTheUserIsLoggedInAndTheUserHasApps() {
        User user = givenAUser(true);

        authenticateUser(session, user);
        Result result = getUser(session);

        assertContainsPublicUserInfo(result, user);
    }

    private void assertContainsPublicUserInfo(Result result, User user) {
        assertEquals(OK, result.status());
        PublicUser publicUser = new UserToPublicUserMapper().map(user, user.getOrganizations().get(0));
        String expectedResponseBody = Json.toJson(publicUser).toString();
        assertEquals(expectedResponseBody, contentAsString(result));
    }

    private User authenticateUser(Session session, User user) {
        UUID uuid = user.getId();
        AuthUser authUser = mock(AuthUser.class);
        session.put("pa.u.id", uuid.toString());
        session.put("pa.u.exp", String.valueOf(Long.MAX_VALUE));
        when(authUser.getId()).thenReturn(uuid.toString());
        when(auth.getUser(session)).thenReturn(authUser);
        when(userRepository.getById(uuid)).thenReturn(user);
        when(userRepository.getByAuthUserIdentity(any(AuthUserIdentity.class))).thenReturn(user);
        return user;
    }

    private User givenAUser(boolean hasApplications) {
        UUID uuid = UUID.randomUUID();
        User user = new User();
        user.setName("Name");
        user.setEmail("email@gmail.com");
        user.setActive(true);
        user.setId(uuid);
        List<Organization> organizations = new LinkedList<>();
        Organization org = new Organization();
        organizations.add(org);
        if (hasApplications) {
            LinkedList<models.Application> applications = new LinkedList<>();
            applications.add(new models.Application());
            org.setApplications(applications);
        }
        user.setOrganizations(organizations);
        return user;
    }

    private Result getUser() {
        return getUser(session);
    }

    private Result getUser(Session session) {
        Http.RequestBuilder requestBuilder = fakeRequest("GET", "/user")
                .session(session);
        return route(requestBuilder);
    }

}
