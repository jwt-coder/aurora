package com.aurora.util;

import com.aurora.entity.Job;
import com.aurora.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class JobInvokeUtil {

    /**
     * 允许通过 Spring bean 名调用的白名单（与数据库中已配置的定时任务保持一致，如 auroraQuartz.statisticalUserArea）
     */
    private static final Set<String> ALLOWED_BEAN_NAMES = new HashSet<>(Arrays.asList("auroraQuartz"));

    public static void invokeMethod(Job job) throws Exception {
        String invokeTarget = job.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        // 白名单校验，防止任意 bean/类被定时任务反射调用
        checkInvokeTarget(beanName);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        if (!isValidClassName(beanName)) {
            Object bean = SpringUtil.getBean(beanName);
            invokeMethod(bean, methodName, methodParams);
        } else {
            Object bean = Class.forName(beanName).newInstance();
            invokeMethod(bean, methodName, methodParams);
        }
    }

    /**
     * 类名方式额外禁止的敏感类：工具类自身与 SpringUtil 等不具备业务语义、被滥用于反射调用的类
     */
    private static final List<String> FORBIDDEN_CLASS_NAME_KEYWORDS = Arrays.asList("SpringUtil", "JobInvokeUtil");

    /**
     * 校验调用目标是否合法：
     * 1. bean 名方式：必须在白名单内；
     * 2. 全限定类名方式：仅允许 com.aurora 包下的类，禁止 java./javax. 等 JDK 内部类，
     *    并禁止 SpringUtil、JobInvokeUtil 等敏感工具类。
     * 不合法直接抛出业务异常。
     */
    private static void checkInvokeTarget(String beanName) {
        if (StringUtils.isNotBlank(beanName)) {
            if (ALLOWED_BEAN_NAMES.contains(beanName)) {
                return;
            }
            // 类名方式只允许 com.aurora 包下，且排除 java./javax. 开头以及敏感工具类
            if (beanName.startsWith("com.aurora.")
                    && !beanName.startsWith("java.")
                    && !beanName.startsWith("javax.")
                    && FORBIDDEN_CLASS_NAME_KEYWORDS.stream().noneMatch(beanName::contains)) {
                return;
            }
        }
        throw new BizException("定时任务调用目标不合法: " + beanName);
    }

    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (methodParams != null && methodParams.size() > 0) {
            Method method = bean.getClass().getDeclaredMethod(methodName, getMethodParamsType(methodParams));
            method.invoke(bean, getMethodParamsValue(methodParams));
        } else {
            Method method = bean.getClass().getDeclaredMethod(methodName);
            method.invoke(bean);
        }
    }

    public static boolean isValidClassName(String invokeTarget) {
        return StringUtils.countMatches(invokeTarget, ".") > 1;
    }

    public static String getBeanName(String invokeTarget) {
        String beanName = StringUtils.substringBefore(invokeTarget, "(");
        return StringUtils.substringBeforeLast(beanName, ".");
    }

    public static String getMethodName(String invokeTarget) {
        String methodName = StringUtils.substringBefore(invokeTarget, "(");
        return StringUtils.substringAfterLast(methodName, ".");
    }

    public static List<Object[]> getMethodParams(String invokeTarget) {
        String methodStr = StringUtils.substringBetween(invokeTarget, "(", ")");
        if (StringUtils.isEmpty(methodStr)) {
            return null;
        }
        String[] methodParams = methodStr.split(",");
        List<Object[]> classes = new LinkedList<>();
        for (String methodParam : methodParams) {
            String str = StringUtils.trimToEmpty(methodParam);
            if (StringUtils.contains(str, "'")) {
                classes.add(new Object[]{StringUtils.replace(str, "'", ""), String.class});
            } else if (StringUtils.equals(str, "true") || StringUtils.equalsIgnoreCase(str, "false")) {
                classes.add(new Object[]{Boolean.valueOf(str), Boolean.class});
            } else if (StringUtils.containsIgnoreCase(str, "L")) {
                classes.add(new Object[]{Long.valueOf(StringUtils.replaceIgnoreCase(str, "L", "")), Long.class});
            } else if (StringUtils.containsIgnoreCase(str, "D")) {
                classes.add(new Object[]{Double.valueOf(StringUtils.replaceIgnoreCase(str, "D", "")), Double.class});
            } else {
                classes.add(new Object[]{Integer.valueOf(str), Integer.class});
            }
        }
        return classes;
    }

    public static Class<?>[] getMethodParamsType(List<Object[]> methodParams) {
        Class<?>[] classes = new Class<?>[methodParams.size()];
        int index = 0;
        for (Object[] os : methodParams) {
            classes[index] = (Class<?>) os[1];
            index++;
        }
        return classes;
    }

    public static Object[] getMethodParamsValue(List<Object[]> methodParams) {
        Object[] classes = new Object[methodParams.size()];
        int index = 0;
        for (Object[] os : methodParams) {
            classes[index] = os[0];
            index++;
        }
        return classes;
    }
}
