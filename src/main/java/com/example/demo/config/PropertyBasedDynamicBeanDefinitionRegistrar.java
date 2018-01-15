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
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * The class is used for dynamically initializing Property beans and dependent consumer, if any.
 * 
 * <p>
 * E.g., lets consider
 * that the application requires below set of config properties for defining
 * connection to a gemfire cluster:
 * </p>
 * <ul>
 * <li>hosts</li>
 * <li>user</li>
 * <li>password</li>
 * </ul>
 * A java vo class is written as (please note that the dyna property setter is required):
 * <pre>
 * {@code
 * public class GfConnection {
 *    	private String[] hosts;
 *    	private String user;
 *    	private String password;
 *    
 *    	// setters
 *    	// getters
 * }
 * </pre>
 * 
 * Now, lets say, user wants to have two gemfire cluster connection configuration.
 * The properties would be defined as:
 * <pre>
 * {@code
 * cluster1.hosts=host1,host2
 * cluster1.user=user1
 * cluster1.password=password1
 * 
 * cluster2.hosts=host3,host4
 * cluster2.user=user2
 * cluster2.password=password2
 * }
 * </pre>
 * 
 * This would mean, two beans of type GfConnection are required, one for cluster1 and another for cluster2.
 * In order to do so, follow below steps:
 * <br>
 * <p># 1: Define property that stores the dynamic property keys (cluster1, cluster2, etc.), in addition to
 * above properties
 * <pre>
 * {@code
 * gf.cluster.keys=cluster1,cluster2
 * }
 * </pre>
 * </p>
 * <p># 2: In Application.java or any of your {@literal @}Configuration annotated class add below bean constructor method as:
 * <pre>
 * {@code}
 * {@literal @}Bean
 *   public DynaPropertyBeanDefinitionRegistrar gfConnRegistrar() {
 *   	return new DynaPropertyBeanInitializer(GfConnection.class, "gfconn", "gf.cluster.keys");
 *   }
 * </pre>
 * </p>
 * <p>
 * That's it. You'll have two beans of type GfConnection, named, gfconnCluster1 and gfconnCluster2 registered in context.
 * </p>
 * <p>
 * Additionally, if you want to dynamically initialize consumer bean that depend upon above property bean instances,
 * e.g., in above example, against each GfConnection instance, if you want to have a GfConnectionConsumer, you'll have to:
 * </p>
 * <p>
 * #1 : Create a consumer class GfConnectionConsumer with an instance variable of type GfConnection along with it's setter
 * and getter, as <b>(make sure, you don't mark the class as {@literal @}Component)</b>:
 * <pre>
 * {@code
 * 	public class GfConnectionConsumer {
 * 		private GfConnection conn;
 * 		public void setConn(GfConnection conn) {
 * 			this.conn=conn;
 * 		}
 * 		public GfConnection getConn() {
 * 			return conn;
 * 		}
 * 	}
 * </pre>
 * </p>
 * <p>
 * #2 : In Application.java or any of your {@literal @}Configuration annotated class add below bean constructor method as:
 * <pre>
 * {@code}
 * {@literal @}Bean
 *   public DynaPropertyBeanDefinitionRegistrar gfConnRegistrar(String[] args) {
 *   	DynaPropertyBeanInitializer initializer =
 *   		new DynaPropertyBeanInitializer(GfConnection.class, "gfconn", "gf.cluster.keys");
 *   	initializer.setPropertyConsumer(GfConnectionConsumer.class, "gfConsumer");
 *   	return initializer;
 *   }
 * </pre>
 * </p>
 * <p>
 * That's it. You'll have two beans of type GfConnectionConsumer, named, gfconsumerCluster1 and gfconsumerCluster2 registered in context.
 * </p>
 */
public class PropertyBasedDynamicBeanDefinitionRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, BeanFactoryAware {

	private ConfigurableEnvironment environment;

	private ConfigurableBeanFactory beanFactory;

	private final Class<?> propertyConfigurationClass;

	private final String propertyBeanNamePrefix;

	private String propertyBeanNameSuffix;

	private final String propertyKeysPropertyName;
	
	private Class<?> propertyConsumerBean;

	private String consumerBeanPropertyFieldName;

	private String consumerBeanNamePrefix;

	private String consumerBeanNameSuffix;

	public PropertyBasedDynamicBeanDefinitionRegistrar(Class<?> propertyConfigurationClass,
                                                       String propertyBeanNamePrefix, String propertyKeysPropertyName) {
		this.propertyConfigurationClass = propertyConfigurationClass;
		this.propertyBeanNamePrefix = propertyBeanNamePrefix;
		this.propertyKeysPropertyName = propertyKeysPropertyName;
	}

	public void setPropertyConsumerBean(Class<?> propertyConsumerBean, String consumerBeanNamePrefix) {
		this.setPropertyConsumerBean(propertyConsumerBean, consumerBeanNamePrefix, null);
	}

	public void setPropertyConsumerBean(Class<?> propertyConsumerBean, String consumerBeanNamePrefix, String consumerBeanPropertyFieldName) {
		this.propertyConsumerBean = propertyConsumerBean;
		this.consumerBeanNamePrefix = consumerBeanNamePrefix;
		this.consumerBeanPropertyFieldName = consumerBeanPropertyFieldName;
	}

	public void setPropertyBeanNameSuffix(String propertyBeanNameSuffix) {
		this.propertyBeanNameSuffix = propertyBeanNameSuffix;
	}
	
	public void setConsumerBeanNameSuffix(String consumerBeanNameSuffix) {
		this.consumerBeanNameSuffix = consumerBeanNameSuffix;
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
		if (propertyConsumerBean != null) {
			String beanPropertyFieldName = getConsumerBeanPropertyVariable();
			for (Map.Entry<String, String> prop : propertyKeyBeanNameMapping.entrySet()) {
				registerConsumerBean(beanDefRegistry, prop.getKey(), prop.getValue(), beanPropertyFieldName);
			}
		}
	}

	private void registerConsumerBean(BeanDefinitionRegistry beanDefRegistry, String trimmedKey, String propBeanName, String beanPropertyFieldName) {
		String consumerBeanName = getBeanName(consumerBeanNamePrefix, trimmedKey, consumerBeanNameSuffix);
		AbstractBeanDefinition consumerDefinition = preparePropertyConsumerBeanDefinition(propBeanName, beanPropertyFieldName);
		beanDefRegistry.registerBeanDefinition(consumerBeanName, consumerDefinition);
	}

	private void registerPropertyBean(BeanDefinitionRegistry beanDefRegistry, String trimmedKey, String propBeanName) {
		AbstractBeanDefinition propertyBeanDefinition = preparePropertyBeanDefinition(trimmedKey);
		beanDefRegistry.registerBeanDefinition(propBeanName, propertyBeanDefinition);
	}

	private String getConsumerBeanPropertyVariable() throws IllegalArgumentException {
		if (consumerBeanPropertyFieldName != null) {
			return consumerBeanPropertyFieldName;
		}
		Field consumerBeanField = ReflectionUtils.findField(propertyConsumerBean, null, propertyConfigurationClass);
		if (consumerBeanField == null) {
			throw new BeanCreationException(String.format("Could not find property of type %s in bean class %s",
					propertyConfigurationClass.getName(), propertyConsumerBean.getName()));
		}
		return consumerBeanField.getName();
	}

	private AbstractBeanDefinition preparePropertyBeanDefinition(String trimmedKey) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(PropertiesConfigurationFactory.class);
		bdb.addConstructorArgValue(propertyConfigurationClass);
		bdb.addPropertyValue("propertySources", environment.getPropertySources());
		bdb.addPropertyValue("conversionService", environment.getConversionService());
		bdb.addPropertyValue("targetName", trimmedKey);
		if (isRefreshScopeEnabled()) {
			bdb.setScope("refresh");
		}
		return bdb.getBeanDefinition();
	}

	private AbstractBeanDefinition preparePropertyConsumerBeanDefinition(String propBeanName, String beanPropertyFieldName) {
		BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(propertyConsumerBean);
		bdb.addPropertyReference(beanPropertyFieldName, propBeanName);
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
