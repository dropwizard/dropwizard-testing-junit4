package io.dropwizard.testing.junit;

import io.dropwizard.testing.junit.app.PeopleStore;
import io.dropwizard.testing.junit.app.Person;
import io.dropwizard.testing.junit.app.PersonResource;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ResourceTestRule}.
 */
public class ResourceTestRuleTest {
    private static class DummyExceptionMapper implements ExceptionMapper<WebApplicationException> {
        @Override
        public Response toResponse(WebApplicationException e) {
            throw new UnsupportedOperationException();
        }
    }

    @Mock
    private PeopleStore peopleStore;

    private final Person person = new Person("blah", "blah@example.com");
    private final MockitoTestRule mockitoTestRule = new MockitoTestRule(this, MockitoJUnit.rule());
    private final ResourceTestRule resourceTestRule = ResourceTestRule.builder()
            .addResource(() -> new PersonResource(peopleStore))
            .setClientConfigurator(cc -> cc.register(DummyExceptionMapper.class))
            .build();

    @Rule
    public final RuleChain ruleChain = RuleChain.outerRule(mockitoTestRule).around(resourceTestRule);

    @Before
    public void setup() {
        when(peopleStore.fetchPerson("blah")).thenReturn(person);
    }

    @Test
    public void testGetPerson() {
        assertThat(resourceTestRule.target("/person/blah").request()
                .get(Person.class))
                .isEqualTo(person);
        verify(peopleStore).fetchPerson("blah");
    }

    @Test
    public void testGetImmutableListOfPersons() {
        Assertions.assertThat(resourceTestRule.target("/person/blah/list").request()
                .get(new GenericType<List<Person>>() {
                })).isEqualTo(Collections.singletonList(person));
    }

    @Test
    public void testGetPersonWithQueryParam() {
        // Test to ensure that the dropwizard validator is registered so that
        // it can validate the "ind" IntParam.
        assertThat(resourceTestRule.target("/person/blah/index")
                .queryParam("ind", 0).request()
                .get(Person.class))
                .isEqualTo(person);
        verify(peopleStore).fetchPerson("blah");
    }

    @Test
    public void testDefaultConstraintViolation() {
        assertThat(resourceTestRule.target("/person/blah/index")
                .queryParam("ind", -1).request()
                .get().readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param ind must be greater than or equal to 0\"]}");
    }

    @Test
    public void testDefaultJsonProcessingMapper() {
        assertThat(resourceTestRule.target("/person/blah/runtime-exception")
                .request()
                .post(Entity.json("{ \"he: \"ho\"}"))
                .readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"Unable to process JSON\"}");
    }

    @Test
    public void testDefaultExceptionMapper() {
        assertThat(resourceTestRule.target("/person/blah/runtime-exception")
                .request()
                .post(Entity.json("{}"))
                .readEntity(String.class))
                .startsWith("{\"code\":500,\"message\":\"There was an error processing your request. It has been logged");
    }

    @Test
    public void testDefaultEofExceptionMapper() {
        assertThat(resourceTestRule.target("/person/blah/eof-exception")
                .request()
                .get().getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testValidationGroupsException() {
        final Response resp = resourceTestRule.target("/person/blah/validation-groups-exception")
                .request()
                .post(Entity.json("{}"));
        assertThat(resp.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(resp.readEntity(String.class))
                .isEqualTo("{\"code\":500,\"message\":\"Parameters must have the same" +
                        " validation groups in validationGroupsException\"}");
    }

    @Test
    public void testCustomClientConfiguration() {
        assertThat(resourceTestRule.client().getConfiguration().isRegistered(DummyExceptionMapper.class)).isTrue();
    }
}
