package com.example.demo.refresh;

import org.springframework.beans.factory.annotation.Value;

public class TestProp {

    @Value("${newTest.firstname}")
    private String firstname;

    @Value("${newTest.lastname}")
    private String lastname;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
