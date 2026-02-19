package com.navn.securitydemo.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class LifecycleBean implements
        BeanNameAware,
        BeanFactoryAware,
        ApplicationContextAware,
        InitializingBean,
        DisposableBean {

    private final Dep dep;

    // 1) Constructor
    public LifecycleBean(Dep dep) {
        this.dep = dep; // 2) DI happens before this constructor is called for constructor injection
        System.out.println("1. [LifecycleBean] constructor (dep injected via constructor)");
    }

    // 3) BeanNameAware
    @Override
    public void setBeanName(String name) {
        System.out.println("3. [LifecycleBean] BeanNameAware.setBeanName() -> " + name);
    }

    // 4) BeanFactoryAware
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("4. [LifecycleBean] BeanFactoryAware.setBeanFactory()");
    }

    // 5) ApplicationContextAware
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("5. [LifecycleBean] ApplicationContextAware.setApplicationContext()");
    }

    // 7) @PostConstruct
    @PostConstruct
    public void postConstruct() {
        System.out.println("7. [LifecycleBean] @PostConstruct");
    }

    // 8) InitializingBean
    @Override
    public void afterPropertiesSet() {
        System.out.println("8. [LifecycleBean] InitializingBean.afterPropertiesSet()");
    }

    // 11) @PreDestroy
    @PreDestroy
    public void preDestroy() {
        System.out.println("11. [LifecycleBean] @PreDestroy");
    }

    // 12) DisposableBean
    @Override
    public void destroy() {
        System.out.println("12. [LifecycleBean] DisposableBean.destroy()");
    }

    public void doWork() {
        System.out.println("👉 Bean Ready: [LifecycleBean] doing work with dep=" + dep.getClass().getSimpleName());
    }
}
