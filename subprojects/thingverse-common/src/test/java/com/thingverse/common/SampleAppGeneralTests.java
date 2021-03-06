/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.common;

import com.thingverse.common.env.health.EnvironmentHealth;
import com.thingverse.common.env.health.EnvironmentHealthListener;
import com.thingverse.common.log.DeferredLogActivator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest
@TestPropertySource(locations = {"classpath:application-test.properties"})
public class SampleAppGeneralTests implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppGeneralTests.class);

    @EnvironmentHealth
    private boolean appIsHealthy;

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        Assert.assertFalse(FAILURE_CHAR + "Expected app to be in unhealthy state.", appIsHealthy);
        LOGGER.info(SUCCESS_CHAR + "App was found to be in expected state - DOWN.");
    }

    @Test
    public void validateMandatoryBeansAvailabilityTest() {
        validateBeanExistence(DeferredLogActivator.class, EnvironmentHealthListener.class);
        LOGGER.info(SUCCESS_CHAR + "Found required beans that should have been created due to auto-configuration.");
    }

    public void validateBeanCreationSuppressionTest() {
        Assert.assertFalse(FAILURE_CHAR + "Did not expect bean name 'someDummyBean' to be available",
                context.containsBean("someDummyBean"));
        LOGGER.info(SUCCESS_CHAR + "Bean named 'someDummyBean' was safely avoided due to unhealthy conditions.");
    }

    @Test
    public void validateDeferredLogActivatorOrderTest() {
        DeferredLogActivator deferredLogActivator = context.getBean(DeferredLogActivator.class);
        Assert.assertEquals(FAILURE_CHAR + "Did not get expected order on the bean",
                Ordered.HIGHEST_PRECEDENCE + 1, deferredLogActivator.getOrder());
        LOGGER.info(SUCCESS_CHAR + "Found {} to have the correct order", deferredLogActivator.getClass().getName());
    }

    private void validateBeanExistence(Class<?>... types) {
        Arrays.stream(types).forEach(t -> {
            if (context.getBeanNamesForType(t).length == 0) {
                Assert.fail(String.format("Bean of type %s was not found", t.getName()));
            }
        });
    }
}
