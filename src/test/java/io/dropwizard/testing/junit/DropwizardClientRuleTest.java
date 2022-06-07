package io.dropwizard.testing.junit;

import io.dropwizard.testing.junit.app.TestResource;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardClientRuleTest {
    @ClassRule
    public static final DropwizardClientRule RULE_WITH_INSTANCE = new DropwizardClientRule(new TestResource("foo"));

    @ClassRule
    public static final DropwizardClientRule RULE_WITH_CLASS = new DropwizardClientRule(TestResource.class);

    @Test
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        try (InputStream inputStream = new URL(RULE_WITH_INSTANCE.baseUri() + "/test").openStream()) {
            assertThat(inputStream).asString(UTF_8).isEqualTo("foo");
        }
    }

    @Test
    public void shouldGetDefaultStringBodyFromDropWizard() throws IOException {
        try (InputStream inputStream = new URL(RULE_WITH_CLASS.baseUri() + "/test").openStream()) {
            assertThat(inputStream).asString(UTF_8).isEqualTo(TestResource.DEFAULT_MESSAGE);
        }
    }
}
