package com.will.ioc_di.v2.beans;

import com.will.ioc_di.v2.core.FactoryBean;

public class BeanWrapper extends FactoryBean {

    // 还会用到 观察者模式
    // 支持事件响应，会有一个监听
    public BeanPostProcessor postProcessor;

    private Object wrapperInstance;
    // 原始的通过反射创建出来的对象，需要保存下来
    private Object originalInstance;

    public BeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.originalInstance = instance;
    }

    // 返回代理以后的Class
    public Class<?> getWrappedClass() {
        return this.wrapperInstance.getClass();
    }

    public BeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(BeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }
}
