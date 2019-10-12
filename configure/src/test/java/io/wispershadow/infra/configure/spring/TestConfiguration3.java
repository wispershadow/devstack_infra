package io.wispershadow.infra.configure.spring;

import org.springframework.beans.factory.annotation.Value;

public class TestConfiguration3 {
    private String property1;

    public String getProperty1() {
        return property1;
    }

    @Value("${core.additional.value1}")
    public void setProperty1(String property1) {
        this.property1 = property1;
    }
}
