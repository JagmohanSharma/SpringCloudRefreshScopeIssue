package com.example.demo.config;

import com.example.demo.refresh.TestProp;
import com.example.demo.refresh.TestService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(TestConfiguration.SERVICE_ACCESS_PROP_KEY)
@AutoConfigureAfter(RefreshAutoConfiguration.class)
public class TestConfiguration {

    public static final String SERVICE_ACCESS_PROP_KEY = "test.dynaProp.keys";

    private static final String REST_TEMPLATE_PROPERTY_BEAN_SUFFIX = "RestTemplateConf";


    @Bean
    public PropertyBasedDynamicBeanDefinitionRegistrar restTemplateRegistrar() {
        PropertyBasedDynamicBeanDefinitionRegistrar registrar = new PropertyBasedDynamicBeanDefinitionRegistrar(TestProperties.class, null, SERVICE_ACCESS_PROP_KEY);
        registrar.setPropertyBeanNameSuffix(REST_TEMPLATE_PROPERTY_BEAN_SUFFIX);
        return registrar;
    }

    @ConfigurationProperties(prefix = "default")
    @Bean
    public TestProperties defaultTestProperties() {
        return new TestProperties();
    }

    @Bean
//    @RefreshScope
    public TestService testService() {
        return new TestService();
    }

    @Bean
    @RefreshScope
    public TestProp testProp() {
        return new TestProp();
    }

}
