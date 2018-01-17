package com.example.demo.refresh;

import com.example.demo.config.TestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class TestService implements ApplicationEventPublisherAware {

    private static final String BEAN_PROPERTY_NAME1 = "NEW_USER_AGENT";
    private static final String BEAN_PROPERTY_VALUE1 = "3434";
    private static final String BEAN_PROPERTY_NAME2 = "newfirstname2";
    private static final String BEAN_PROPERTY_VALUE2 = "newlastname2";

    @Autowired
    private TestProp testProp;
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource(name = "service1RestTemplateConf")
    private TestProperties service1RestTemplateConf;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    public String getServices() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
       return stringBuilder.append(" first name(@RefreshScope): " + testProp.getFirstname()).
                append("<br/>").append(" UserAgent(BDRPP): " + service1RestTemplateConf.getUserAgent()).append("<br/>").
                append(" connect timeout(BDRPP): " + service1RestTemplateConf.getTimeout().getConnect()).toString();
    }

    public void updateEnv() {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        PropertySource propertySource = prepareNewPropertySource();
        propertySources.addFirst(propertySource);

        applicationEventPublisher.publishEvent(new RefreshEvent(this, propertySource, propertySource.getName()));
    }

    private PropertySource prepareNewPropertySource() {
        Map<String, Object> map = new HashMap<>();
        map.put("service1.userAgent", BEAN_PROPERTY_NAME1);
        map.put("service1.timeout.connect", BEAN_PROPERTY_VALUE1);
        map.put("newTest.firstname", BEAN_PROPERTY_NAME2);
        map.put("newTest.lastname", BEAN_PROPERTY_VALUE2);
        PropertySource newPropertySource = new MapPropertySource("NewPropertySource", map);
        return newPropertySource;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
