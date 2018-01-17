package com.example.demo.controller;

import com.example.demo.refresh.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rest/v1")
public class TestController {

    @Autowired
    private TestService testService;

    @RequestMapping(method = RequestMethod.GET, value = "/getProp2")
    public String getProp2() throws Exception {
        return testService.getServices();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/updateEnv")
    public void updateEnv() throws Exception {
        testService.updateEnv();
    }

}
