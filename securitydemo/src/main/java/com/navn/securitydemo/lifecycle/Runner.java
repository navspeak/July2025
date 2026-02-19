package com.navn.securitydemo.lifecycle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {

    private final LifecycleBean lifecycleBean;

    public Runner(LifecycleBean lifecycleBean) {
        this.lifecycleBean = lifecycleBean;
        System.out.println("2. [Runner] dependency injection complete (LifecycleBean injected)");
    }

    @Override
    public void run(String... args) throws Exception {
        lifecycleBean.doWork();
        System.out.println("Press Ctrl+C to trigger shutdown callbacks...");
    }
}

