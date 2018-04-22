package com.will.ioc_di.v2.core;

public interface BeanFactory {

    /**
     * 根据beanName从IOC容器中获得一个实例Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}
