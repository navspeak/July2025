package com.navn.securitydemo.lifecycle;
import org.springframework.stereotype.Component;

@Component
public class Dep {
    public Dep() {
        System.out.println("   [Dep] constructor");
    }
}
