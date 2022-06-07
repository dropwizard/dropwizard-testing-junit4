package io.dropwizard.testing.junit.app;

public interface PeopleStore {
    Person fetchPerson(String name);
}
