package com.navn.securitydemo.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LifecycleConfig {

    // 9) custom init-method
    // 13) custom destroy-method
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public CustomInitDestroyBean customInitDestroyBean() {
        return new CustomInitDestroyBean();
    }

    public static class CustomInitDestroyBean {
        public CustomInitDestroyBean() {
            System.out.println("   [CustomInitDestroyBean] constructor");
        }

        public void customInit() {
            System.out.println("9.  [CustomInitDestroyBean] custom init-method");
        }

        public void customDestroy() {
            System.out.println("13. [CustomInitDestroyBean] custom destroy-method");
        }
    }
}
