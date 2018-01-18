package com.example.demo.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.validation.BindException;

public class PropertyBasedDynamicBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TestProperties) {
            PropertiesConfigurationFactory<Object> binder = new PropertiesConfigurationFactory<Object>(
                    bean);
            binder.setTargetName("service1");
            binder.setConversionService(this.environment.getConversionService());
            binder.setPropertySources(environment.getPropertySources());
            try {
                binder.bindPropertiesToTarget();
            } catch (BindException ex) {
                throw new IllegalStateException("Cannot bind to TestProperties", ex);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
