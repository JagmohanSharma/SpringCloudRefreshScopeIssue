# SpringCloudRefreshScopeIssue

 The class is used for dynamically initializing Property beans and dependent consumer, if any.
 
 
 Now we are only registering a bean using BDRPP and setting its scope as `refresh`but at the time of RefreshEvent we are not getting this bean refreshed.
 
 Two end points:
 1) 
 /rest/v1/getProp 
 this will return original property values read from `application.properties`:
 TestProp(`@RefreshScope`) first name               `newFIrstName`
   service1RestTemplateConf(`BDRPP`) UserAgent       `TEST_USER_AGENT`
  service1RestTemplateConf(`BDRPP`) connect timeout   `4343`
 
 2)
 /rest/v1/updateEnv
 
 This will add new property source to environment and publish RefreshEvent which should refresh beans as well
 but when we again hit
 /rest/v1/getProp
 
 TestProp(`@RefreshScope`) first name               `newfirstname2`
  service1RestTemplateConf(`BDRPP`) UserAgent       `TEST_USER_AGENT`
 service1RestTemplateConf(`BDRPP`) connect timeout   `4343`
 
 
 As we can see UserAgent and connect timeout values are not updated since its bean `service1RestTemplateConf` is not getting refreshed.
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
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
