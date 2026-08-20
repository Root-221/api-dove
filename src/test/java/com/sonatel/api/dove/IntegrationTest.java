package com.sonatel.api.dove;

import com.sonatel.api.dove.config.AsyncSyncConfiguration;
import com.sonatel.api.dove.config.EmbeddedSQL;
import com.sonatel.api.dove.config.JacksonConfiguration;
import com.sonatel.api.dove.config.RedisTestContainer;
import com.sonatel.api.dove.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        ApidoveApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        TestSecurityConfiguration.class,
        com.sonatel.api.dove.config.JacksonHibernateConfiguration.class,
    }
)
@EmbeddedSQL
@ImportTestcontainers(RedisTestContainer.class)
public @interface IntegrationTest {}
