package com.smartcampus.api;

import com.smartcampus.api.store.DataStore;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.core.Application;

/**
 * Base class for all resource tests.
 *
 * ROOT CAUSE OF "Address already in use":
 * JerseyTest is JUnit 4. JUnit 5 does NOT invoke JUnit 4 @Before/@After
 * lifecycle methods on parent classes. So setUp() runs but tearDown() never
 * fires — every test leaks a Grizzly server, and the next test fails to bind.
 *
 * FIX: @TestInstance(PER_CLASS) + @BeforeAll/@AfterAll
 * One Grizzly server starts per test CLASS (not per test method).
 * @BeforeEach only clears the DataStore so each test starts with a clean slate.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest extends JerseyTest {

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new SmartCampusApplication();
    }

    @BeforeAll
    public void startServer() throws Exception {
        setUp();
    }

    @AfterAll
    public void stopServer() throws Exception {
        tearDown();
    }

    @BeforeEach
    public void resetData() {
        DataStore.getInstance().clear();
    }
}
