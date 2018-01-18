package com.example.demo.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;


public class PropertyBasedDynamicBeanDefinitionRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, BeanFactoryAware {

	private ConfigurableEnvironment environment;

	private ConfigurableBeanFactory beanFactory;

	private final Class<?> propertyConfigurationClass;

	private final String propertyBeanNamePrefix;

	private String propertyBeanNameSuffix;

	private final String propertyKeysPropertyName;

	public PropertyBasedDynamicBeanDefinitionRegistrar(Class<?> propertyConfigurationClass,
                                                       String propertyBeanNamePrefix, String propertyKeysPropertyName) {
		this.propertyConfigurationClass = propertyConfigurationClass;
		this.propertyBeanNamePrefix = propertyBeanNamePrefix;
		this.propertyKeysPropertyName = propertyKeysPropertyName;
	}

	public void setPropertyBeanNameSuffix(String propertyBeanNameSuffix) {
		this.propertyBeanNameSuffix = propertyBeanNameSuffix;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = (ConfigurableEnvironment) environment;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory arg0) throws BeansException {

	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefRegistry) throws BeansException {
		String[] keys = getPropertyKeys();
		Map<String, String> propertyKeyBeanNameMapping = new HashMap<>();
		for (String k : keys) {
			String trimmedKey = k.trim();
			if (StringUtils.isEmpty(trimmedKey)) {
				continue;
			}
			String propBeanName = getBeanName(propertyBeanNamePrefix, trimmedKey, propertyBeanNameSuffix);
			registerPropertyBean(beanDefRegistry, trimmedKey, propBeanName);
			propertyKeyBeanNameMapping.put(trimmedKey, propBeanName);
		}

	}

	private void registerPropertyBean(BeanDefinitionRegistry beanDefRegistry, String trimmedKey, String propBeanName) {
		AbstractBeanDefinition propertyBeanDefinition = preparePropertyBeanDefinition(trimmedKey);
		beanDefRegistry.registerBeanDefinition(propBeanName, propertyBeanDefinition);
	}

	private AbstractBeanDefinition preparePropertyBeanDefinition(String trimmedKey) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(propertyConfigurationClass);
		if (isRefreshScopeEnabled()) {
			bdb.setScope("refresh");
		}
		return bdb.getBeanDefinition();
	}


	private String getBeanName(String prefix, String key, String suffix) {
		StringBuilder beanNameBuilder = new StringBuilder();
		if (StringUtils.isNotEmpty(prefix)) {
			beanNameBuilder.append(prefix);
			beanNameBuilder.append(key.substring(0, 1).toUpperCase());
			beanNameBuilder.append(key.substring(1));
		} else {
			beanNameBuilder.append(key);
		}
		if (StringUtils.isNotEmpty(suffix)) {
			beanNameBuilder.append(suffix);
		}
		return beanNameBuilder.toString();
	}

	private String[] getPropertyKeys() {
		String keysProp = environment.getProperty(propertyKeysPropertyName);
		if (StringUtils.isEmpty(keysProp)) {
			throw new BeanCreationException(String.format("Property % not found or is empty", propertyKeysPropertyName));
		}
		return keysProp.split(",");
	}

	private boolean isRefreshScopeEnabled() {
		try {
			return beanFactory.getBean("refreshScope") != null;
		} catch (BeansException exp) {
			return false;
		}
	}

}
