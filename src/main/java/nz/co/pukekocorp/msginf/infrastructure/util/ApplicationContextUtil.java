package nz.co.pukekocorp.msginf.infrastructure.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Helper class to get beans from the Spring application context.
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {

    /**
     * The Spring application context.
     */
    private static ApplicationContext context;

    /**
     * Get the Spring application context.
     * @return the Spring application context.
     */
    public static ApplicationContext getContext() {
        return context;
    }

    /**
     * Get a bean from the Spring application context.
     * @param bean the bean class.
     * @return the bean.
     * @param <T> the bean type.
     */
    public static <T> T getBean(Class<T> bean) {
        return getContext().getBean(bean);
    }

    /**
     * Get a bean from the Spring application context.
     * @param beanName the bean name.
     * @param bean the bean class.
     * @return the bean.
     * @param <T> the bean type.
     */
    public static <T> T getBean(String beanName, Class<T> bean) {
        return getContext().getBean(beanName, bean);
    }

    /**
     * Set the Spring application context.
     * @param applicationContext the Spring application context.
     * @throws BeansException the beans exception.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
