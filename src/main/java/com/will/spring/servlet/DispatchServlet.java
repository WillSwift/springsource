package com.will.spring.servlet;

import com.will.demo.mvc.action.DemoAction;
import com.will.spring.annotation.Autowired;
import com.will.spring.annotation.Controller;
import com.will.spring.annotation.Service;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();
    private List<String> classNames = new ArrayList<String>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("---调用doPost---");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 开始初始化进程

        // 定位
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 加载
        doScanner(contextConfig.getProperty("scanPackage"));

        // 注册
        doRegistry();

        // 自动依赖注入
        // 在Spring中调用getBean方法时才触发依赖注入
        doAutowired();

        DemoAction demoAction = (DemoAction)beanMap.get("demoAction");
        demoAction.query(null, null, "Will");

    }

    private void doLoadConfig(String location) {
        // 在Spring中是通过Reader去查找和定位的
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:", ""));

        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));

        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                if (!classNames.contains(className)) {
                    classNames.add(className);
                }
            }
        }
    }

    private void doRegistry() {
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                // 在Spring中是用多个子方法来处理
                if (clazz.isAnnotationPresent(Controller.class)) {
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    // 在Spring中在这个阶段不是直接put instance，而是put BeanDefinition
                    beanMap.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);

                    // 默认用首字母小写的类名注入
                    // 如果自己定义了beanName，那么优先使用自己定义的beanName
                    // 如果是一个接口，使用接口的类型去自动注入

                    // 在Spring中同样会分别调用不同的方法： autowiredByName autowiredByType

                    String beanName = service.value();
                    if (StringUtils.isBlank(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    beanMap.put(beanName, instance);

                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        beanMap.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        if (beanMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                Autowired autowired = field.getAnnotation(Autowired.class);
                String beanName = autowired.value();
                if (StringUtils.isBlank(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), beanMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
