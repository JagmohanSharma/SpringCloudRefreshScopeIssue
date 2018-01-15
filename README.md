# SpringCloudRefreshScopeIssue

 The class is used for dynamically initializing Property beans and dependent consumer, if any.
 
 
 E.g., lets consider
 * that the application requires below set of config properties for defining a particular type of bean.
 
 As mentioned in PropertyBasedDynamicBeanDefinitionRegistrarTest 
 
 
 test.beans=bean1, bean2  // this property is first used to fetch bean name using provided suffix and prefix if any.
 
 we have given 
 ```
 private static final String CONSUMER_BEAN_NAME_PREFIX = "testConsumerBean";

  private static final String PROPERTY_BEAN_NAME_PREFIX = "testPropertyBean";

 private static final String CONSUMER_BEAN_NAME_SUFFIX = "ConsumerBeanSuffix";

  private static final String PROPERTY_BEAN_NAME_SUFFIX = "PropertyBeanSuffix";
 ```
 
 So these will be used to create bean names respectively based on provided configuration as
 ```
 testPropertyBeanBean1PropertyBeanSuffix
 testPropertyBeanBean2PropertyBeanSuffix
 testConsumerBeanBean1ConsumerBeanSuffix
 testConsumerBeanBean2ConsumerBeanSuffix
```

In postProcessBeanDefinitionRegistry() method , we first create property beans and register to BeanDefinitionRegistry.

And our consumer bean has a reference to property bean so consumer bean is registered by adding this property reference to beanPropertyFieldName.


This consumer bean is a Factory bean, which provides objects for given type.
