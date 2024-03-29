package io.dropwizard.testing.junit;

import io.dropwizard.core.Application;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.testing.junit.app.TestConfiguration;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class DropwizardAppRuleWithExplicitTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE;

    static {
        // Bit complicated, as we want to avoid using the default http port (8080)
        // as there is another test that uses it already. So force bogus value of
        // 0, similar to what `test-config.yaml` defines.
        TestConfiguration config = new TestConfiguration("stuff!", "extra");
        DefaultServerFactory sf = (DefaultServerFactory) config.getServerFactory();
        ((HttpConnectorFactory) sf.getApplicationConnectors().get(0)).setPort(0);
        ((HttpConnectorFactory) sf.getAdminConnectors().get(0)).setPort(0);
        RULE = new DropwizardAppRule<>(TestApplication.class, config);
    }


    @Test
    public void runWithExplicitConfig() {
        Map<String, String> response = RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/test")
            .request()
            .get(new GenericType<Map<String, String>>() {
            });
        assertThat(response).containsOnly(entry("message", "stuff!"));
    }

    public static class TestApplication extends Application<TestConfiguration> {
        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            environment.jersey().register(new TestResource(configuration.getMessage()));
        }
    }

    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestResource {
        private final String message;

        public TestResource(String m) {
            message = m;
        }

        @GET
        public Response get() {
            return Response.ok(Collections.singletonMap("message", message)).build();
        }
    }
}
