/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.quickstart;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.quickstart.page.ConsolePage;
import org.keycloak.quickstart.storage.user.EjbExampleUserStorageProvider;
import org.keycloak.quickstart.storage.user.EjbExampleUserStorageProviderFactory;
import org.keycloak.quickstart.storage.user.UserAdapter;
import org.keycloak.quickstart.storage.user.UserEntity;
import org.keycloak.test.page.LoginPage;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.keycloak.test.TestsHelper.importTestRealm;


@RunWith(Arquillian.class)
public class ArquillianJpaStorageTest {

    private static final String KEYCLOAK_ADMIN = "http://%s:%s/auth/admin";

    @Page
    private LoginPage loginPage;

    @Page
    private ConsolePage consolePage;

    @Drone
    private WebDriver webDriver;

    @ArquillianResource
    private URL contextRoot;

    @Deployment(testable = false)
    public static Archive<?> createTestArchive() throws IOException {
        return ShrinkWrap.create(JavaArchive.class, "user-storage-jpa-example.jar")
                .addClasses(EjbExampleUserStorageProvider.class, EjbExampleUserStorageProviderFactory.class,
                        UserAdapter.class, UserEntity.class)
                .addAsResource("META-INF/services/org.keycloak.storage.UserStorageProviderFactory")
                .addAsResource("META-INF/persistence.xml");

    }


    @Before
    public void setup() {
        webDriver.navigate().to(format(KEYCLOAK_ADMIN,
                contextRoot.getHost(), contextRoot.getPort()));
    }

    @Test
    public void testUserFederationStorageCreation() throws MalformedURLException, InterruptedException {
        try {
            loginPage.login("admin", "admin");

            consolePage.navigateToUserFederationMenu();
            consolePage.selectUserStorage();
            consolePage.save().click();
            consolePage.navigateToUserFederationMenu();

            assertNotNull("Storage provider should be created", consolePage.exampleFederationStorageLink());

            consolePage.delete();
        } catch (Exception e) {
            fail("Should create a user federation storage");
        }
    }
}
