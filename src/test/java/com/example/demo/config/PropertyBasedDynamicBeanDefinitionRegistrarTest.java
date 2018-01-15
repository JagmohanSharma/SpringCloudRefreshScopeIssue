package com.example.demo.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, PropertyBasedDynamicBeanDefinitionRegistrarTest.DynaPropConfig.class})
@TestPropertySource(properties = {"test.beans=bean1, bean2", "bean1.name=bname1", "bean1.value=bvalue1",
        "bean2.name=bname2", "bean2.value=bvalue2"})
public class PropertyBasedDynamicBeanDefinitionRegistrarTest implements ApplicationEventPublisherAware {

    private static final String CONSUMER_BEAN_NAME_PREFIX = "testConsumerBean";

    private static final String PROPERTY_BEAN_NAME_PREFIX = "testPropertyBean";

    private static final String CONSUMER_BEAN_NAME_SUFFIX = "ConsumerBeanSuffix";

    private static final String PROPERTY_BEAN_NAME_SUFFIX = "PropertyBeanSuffix";

    private static final String PROPERTY_NAME = "test.beans";
    private static final String BEAN_PROPERTY_NAME1 = "newbname1";
    private static final String BEAN_PROPERTY_VALUE1 = "newbvalue1";
    private static final String BEAN_PROPERTY_NAME2 = "newbname2";
    private static final String BEAN_PROPERTY_VALUE2 = "newbvalue2";
    private static final String REFRESH = "refresh";

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private TestDynaPropRefresh testDynaPropRefresh;
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void test() {
        assertEquals(2, testDynaPropRefresh.getTestPropBeans().size());
        assertTrue(testDynaPropRefresh.getTestPropBeans().containsKey("testPropertyBeanBean1PropertyBeanSuffix"));
        assertTrue(testDynaPropRefresh.getTestPropBeans().containsKey("testPropertyBeanBean2PropertyBeanSuffix"));

        assertEquals("bname1", testDynaPropRefresh.getPropBeanName("testPropertyBeanBean1PropertyBeanSuffix"));
        assertEquals("bvalue1", testDynaPropRefresh.getPropBeanValue("testPropertyBeanBean1PropertyBeanSuffix"));
        assertEquals("bname2", testDynaPropRefresh.getPropBeanName("testPropertyBeanBean2PropertyBeanSuffix"));
        assertEquals("bvalue2", testDynaPropRefresh.getPropBeanValue("testPropertyBeanBean2PropertyBeanSuffix"));

        assertEquals(2, testDynaPropRefresh.getTestConsumerBeans().size());
        assertTrue(testDynaPropRefresh.getTestConsumerBeans().containsKey("testConsumerBeanBean1ConsumerBeanSuffix"));
        assertTrue(testDynaPropRefresh.getTestConsumerBeans().containsKey("testConsumerBeanBean2ConsumerBeanSuffix"));
        assertEquals(testDynaPropRefresh.getTestPropBeans().get("testPropertyBeanBean1PropertyBeanSuffix"), testDynaPropRefresh.getTestConsumerBeans().get("testConsumerBeanBean1ConsumerBeanSuffix").getProp());
        assertEquals(testDynaPropRefresh.getTestPropBeans().get("testPropertyBeanBean2PropertyBeanSuffix"), testDynaPropRefresh.getTestConsumerBeans().get("testConsumerBeanBean2ConsumerBeanSuffix").getProp());
        assertEquals("bname1:bvalue1", testDynaPropRefresh.getConsumerBeanProcessedString("testConsumerBeanBean1ConsumerBeanSuffix"));
        assertEquals("bname2:bvalue2", testDynaPropRefresh.getConsumerBeanProcessedString("testConsumerBeanBean2ConsumerBeanSuffix"));

        verifyBeanRefreshScope();

        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        PropertySource propertySource = prepareNewPropertySource();
        propertySources.addFirst(propertySource);

        applicationEventPublisher.publishEvent(new RefreshEvent(this, propertySource, propertySource.getName()));

        verifyBeanRefreshScope();

        assertEquals(2, testDynaPropRefresh.getTestPropBeans().size());
        assertEquals(BEAN_PROPERTY_NAME1, testDynaPropRefresh.getPropBeanName("testPropertyBeanBean1PropertyBeanSuffix"));
        assertEquals(BEAN_PROPERTY_VALUE1, testDynaPropRefresh.getPropBeanValue("testPropertyBeanBean1PropertyBeanSuffix"));
        assertEquals(BEAN_PROPERTY_NAME2, testDynaPropRefresh.getPropBeanName("testPropertyBeanBean2PropertyBeanSuffix"));
        assertEquals(BEAN_PROPERTY_VALUE2, testDynaPropRefresh.getPropBeanValue("testPropertyBeanBean2PropertyBeanSuffix"));
        assertEquals(2, testDynaPropRefresh.getTestConsumerBeans().size());
        assertEquals(BEAN_PROPERTY_NAME1 + ":" + BEAN_PROPERTY_VALUE1, testDynaPropRefresh.getConsumerBeanProcessedString("testConsumerBeanBean1ConsumerBeanSuffix"));
        assertEquals(BEAN_PROPERTY_NAME2 + ":" + BEAN_PROPERTY_VALUE2, testDynaPropRefresh.getConsumerBeanProcessedString("testConsumerBeanBean2ConsumerBeanSuffix"));
    }

    private PropertySource prepareNewPropertySource() {
        Map<String, Object> map = new HashMap<>();
        map.put("bean1.name", BEAN_PROPERTY_NAME1);
        map.put("bean1.value", BEAN_PROPERTY_VALUE1);
        map.put("bean2.name", BEAN_PROPERTY_NAME2);
        map.put("bean2.value", BEAN_PROPERTY_VALUE2);
        PropertySource newPropertySource = new MapPropertySource("NewPropertySource", map);
        return newPropertySource;
    }

    private void verifyBeanRefreshScope() {
        ConfigurableListableBeanFactory bf = applicationContext.getBeanFactory();
        BeanDefinition bd1 = bf.getBeanDefinition("testConsumerBeanBean1ConsumerBeanSuffix");
        String scope = bd1.getScope();
        assertEquals(scope, REFRESH);

        BeanDefinition bd2 = bf.getBeanDefinition("testConsumerBeanBean1ConsumerBeanSuffix");
        scope = bd2.getScope();
        assertEquals(scope, REFRESH);


        BeanDefinition bd3 = bf.getBeanDefinition("testConsumerBeanBean1ConsumerBeanSuffix");
        scope = bd3.getScope();
        assertEquals(scope, REFRESH);


        BeanDefinition bd4 = bf.getBeanDefinition("testConsumerBeanBean1ConsumerBeanSuffix");
        scope = bd4.getScope();
        assertEquals(scope, REFRESH);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Configuration
    @ImportAutoConfiguration({RefreshAutoConfiguration.class})
    public static class DynaPropConfig {

        @Bean
        public PropertyBasedDynamicBeanDefinitionRegistrar registrar() {
            PropertyBasedDynamicBeanDefinitionRegistrar registrar = new PropertyBasedDynamicBeanDefinitionRegistrar(TestDynaProp.class, PROPERTY_BEAN_NAME_PREFIX, PROPERTY_NAME);
            registrar.setPropertyConsumerBean(TestDynaPropConsumer.class, CONSUMER_BEAN_NAME_PREFIX);
            registrar.setConsumerBeanNameSuffix(CONSUMER_BEAN_NAME_SUFFIX);
            registrar.setPropertyBeanNameSuffix(PROPERTY_BEAN_NAME_SUFFIX);
            return registrar;
        }

        @Bean
        @RefreshScope
//        @RefreshScope If I use this , this test works fine.
        public TestDynaPropRefresh testDynaPropRefresh() {
            return new TestDynaPropRefresh();
        }

    }

    public static class TestDynaProp {

        private String name;

        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestDynaProp [name=" + name + ", value=" + value + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestDynaProp other = (TestDynaProp) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

    }

    public static class TestDynaPropConsumer {

        private TestDynaProp prop;

        private String postProcessedString;

        public void setProp(TestDynaProp prop) {
            this.prop = prop;
        }

        public TestDynaProp getProp() {
            return prop;
        }

        public String getPostProcessedString() {
            return postProcessedString;
        }

        @PostConstruct
        public void init() {
            postProcessedString = prop.getName() + ":" + prop.getValue();
        }

    }

    public static class TestDynaPropRefresh {

        @Resource
        private Map<String, TestDynaProp> testPropBeans;

        @Resource
        private Map<String, TestDynaPropConsumer> testConsumerBeans;

        public String getPropBeanName(String beanName) {
            return testPropBeans.get(beanName).getName();
        }

        public String getPropBeanValue(String beanName) {
            return testPropBeans.get(beanName).getValue();
        }

        public String getConsumerBeanProcessedString(String beanName) {
            return testConsumerBeans.get(beanName).getPostProcessedString();
        }

        public Map<String, TestDynaProp> getTestPropBeans() {
            return testPropBeans;
        }

        public Map<String, TestDynaPropConsumer> getTestConsumerBeans() {
            return testConsumerBeans;
        }
    }

}
