package com.example.demo.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class PropertyBasedDynamicBeanPostProcessorRegistrar implements BeanDefinitionRegistryPostProcessor {

    /**
     * The bean name of the {@link PropertyBasedDynamicBeanPostProcessor}.
     */
    public static final String BINDER_BEAN_NAME = PropertyBasedDynamicBeanPostProcessor.class
            .getName();


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!registry.containsBeanDefinition(BINDER_BEAN_NAME)) {
            BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(
                    PropertyBasedDynamicBeanPostProcessor.class);
            registry.registerBeanDefinition(BINDER_BEAN_NAME, bean.getBeanDefinition());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
