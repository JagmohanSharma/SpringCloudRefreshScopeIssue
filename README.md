# SpringCloudRefreshScopeIssue

 The class is used for dynamically initializing Property beans and dependent consumer, if any.
 
 
 Now we are only registering a bean using BDRPP and setting its scope as `refresh`but at the time of RefreshEvent we are not getting this bean refreshed.
 
 Two end points:
 1) 
 
 `/rest/v1/getProp`
 this will return original property values read from `application.properties`:
 
 TestProp(`@RefreshScope`) first name               `newFIrstName` <br />
   service1RestTemplateConf(`BDRPP`) UserAgent       `TEST_USER_AGENT`   <br />
  service1RestTemplateConf(`BDRPP`) connect timeout   `4343` <br />
 
 2)
 `/rest/v1/updateEnv`
 
 This will add new property source to environment and publish RefreshEvent which should refresh beans as well
 but when we again hit
 `/rest/v1/getProp`
 
 TestProp(`@RefreshScope`) first name               `newfirstname2`  <br />
  service1RestTemplateConf(`BDRPP`) UserAgent       `TEST_USER_AGENT` <br />
 service1RestTemplateConf(`BDRPP`) connect timeout   `4343` <br />
 
 
 As we can see UserAgent and connect timeout values are not updated since its bean `service1RestTemplateConf` is not getting refreshed.