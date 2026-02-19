package com.navn.securitydemo.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
public class LifecycleBpp implements BeanPostProcessor {
/* use for Cross cutting concerns */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LifecycleBean) {
            System.out.println("6. [BPP] postProcessBeforeInitialization() -> " + beanName);
        }
        // Only wrap services we care about
        if (!(bean instanceof MonitoredService)) {
            return bean;
        }

        //return a Proxy -> we could do use
//        ✅ @Aspect + @Around is the idiomatic solution
//        ✅ works with @Service, @Component, custom annotations, pointcuts
//        ✅ avoids hand-rolled Proxy.newProxyInstance() and reflection glue
        return Proxy.newProxyInstance(
                bean.getClass().getClassLoader(),
                bean.getClass().getInterfaces(),
                (proxy, method, args) -> {

                    long start = System.currentTimeMillis();

                    Object result = method.invoke(bean, args);

                    long end = System.currentTimeMillis();

                    System.out.println(
                            "⏱ " + beanName + "." + method.getName() +
                                    " took " + (end - start) + " ms"
                    );

                    return result;
                }
        );

    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LifecycleBean) {
            System.out.println("10. [BPP] postProcessAfterInitialization() -> " + beanName);
        }
        return bean;
    }
}

