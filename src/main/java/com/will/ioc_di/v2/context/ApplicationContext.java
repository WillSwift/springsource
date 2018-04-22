package com.will.ioc_di.v2.context;

import com.will.ioc_di.v2.annotation.Autowired;
import com.will.ioc_di.v2.annotation.Controller;
import com.will.ioc_di.v2.annotation.Service;
import com.will.ioc_di.v2.beans.BeanDefinition;
import com.will.ioc_di.v2.beans.BeanPostProcessor;
import com.will.ioc_di.v2.beans.BeanWrapper;
import com.will.ioc_di.v2.context.support.BeanDefinitionReader;
import com.will.ioc_di.v2.core.BeanFactory;
import com.will.ioc_di.v2.demo.action.MyAction;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    // beanDefinitionMap用来保存配置信息
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    // 用来保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new ConcurrentHashMap<>();

    // 用来存储所有被代理过的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();


    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        refresh();
    }

    public void refresh() {
        // 定位
        this.reader = new BeanDefinitionReader(configLocations);

        // 加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();

        // 注册
        doRegistry(beanDefinitions);

        // 依赖注入（lazy-init = false，则容器启动时就初始化并执行依赖注入）
        // 在这里自动调用getBean方法
        doAutowired();

        MyAction myAction = (MyAction)this.getBean("myAction");
        myAction.query(null, null, "Will");
    }

    /**
     * 依赖注入从这里开始，通过读取BeanDefinition中的信息
     * 然后，通过反射机制创建一个实例并返回
     * Spring的做法是：不会把最原始的对象放出去，而是用一个BeanWrapper来进行一次包装
     * 装饰器模式：
     * 1、保留原来的OOP关系
     * 2、需要对他扩展、增强（为以后AOP打基础）
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            return null;
        }
        String className = beanDefinition.getBeanClassName();

        try {
            // 生成通知事件
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
            Object instance = instantiateBean(beanDefinition);
            if (null == instance) {
                return null;
            }

            // 在实例初始化以前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

            BeanWrapper beanWrapper = new BeanWrapper(instance);
            beanWrapper.setPostProcessor(beanPostProcessor);
            this.beanWrapperMap.put(beanName, beanWrapper);

            // 在实例初始化以后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);

            populateBean(beanName, instance);

            // 通过这样一调用，相当于给我们自己留有了可操作的空间
            return this.beanWrapperMap.get(beanName).getWrapperInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 真正将BeanDefinitions注册到BeanDefinitionMap中
     * @param beanDefinitions
     */
    private void doRegistry(List<String> beanDefinitions) {

        // beanName有三种情况：
        // 1.默认是类名首字母小写
        // 2.自定义名字
        // 3.接口注入
        try {
            for (String className : beanDefinitions) {
                Class<?> beanClass = Class.forName(className);

                // 如果是一个接口，是不能实例化的
                // 用它的实现类来实例化
                if (beanClass.isInterface()) {
                    continue;
                }

                BeanDefinition beanDefinition = reader.registerBean(className);
                if (beanDefinition != null) {
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    // 如果是多个实现类，只能覆盖
                    this.beanDefinitionMap.put(lowerFirstCase(i.getSimpleName()), beanDefinition);
                }

                // 到此为止，容器初始化完毕
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始执行自动化的依赖注入
     */
    private void doAutowired() {

        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();

            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                getBean(beanName);
            }
        }

    }

    /**
     * 传一个BeanDefinition，就返回一个实例Bean
     * @param beanDefinition
     * @return
     */
    private Object instantiateBean(BeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            if (beanDefinition.isSingleton()) {
                // 因为根据Class才能确定一个类是否有实例
                if (this.beanCacheMap.containsKey(className)) {
                    instance = this.beanCacheMap.get(className);
                } else {
                    Class<?> clazz = Class.forName(className);
                    instance = clazz.newInstance();
                    this.beanCacheMap.put(className, instance);
                }
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private void populateBean(String beanName, Object instance) {
        Class clazz = instance.getClass();

        if (!clazz.isAnnotationPresent(Controller.class) ||
                clazz.isAnnotationPresent(Service.class)) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowiredBeanName = autowired.value();

            if (StringUtils.isBlank(autowiredBeanName)) {
                autowiredBeanName = lowerFirstCase(field.getType().getSimpleName());
            }

            field.setAccessible(true);

            try {
                if (!beanWrapperMap.containsKey(autowiredBeanName)) {
                    getBean(autowiredBeanName);
                }
                field.set(instance, beanWrapperMap.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
