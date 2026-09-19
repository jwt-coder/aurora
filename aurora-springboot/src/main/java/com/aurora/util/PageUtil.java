package com.aurora.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Objects;

import static com.aurora.constant.CommonConstant.DEFAULT_SIZE;

public class PageUtil {

    private static final ThreadLocal<Page<?>> PAGE_HOLDER = new ThreadLocal<>();

    /**
     * 分页参数安全上限，防止一次拉取过多数据
     */
    private static final long MAX_SIZE = 100L;

    /**
     * 页码安全上限，防止 (current-1)*size 溢出为负数导致 LIMIT 报错
     */
    private static final long MAX_CURRENT = 1_000_000L;

    private static final long DEFAULT_CURRENT = 1L;

    public static void setCurrentPage(Page<?> page) {
        PAGE_HOLDER.set(page);
    }

    public static Page<?> getPage() {
        Page<?> page = PAGE_HOLDER.get();
        if (Objects.isNull(page)) {
            setCurrentPage(new Page<>());
        }
        return PAGE_HOLDER.get();
    }

    public static Long getCurrent() {
        long current = getPage().getCurrent();
        // current < 1 时钳制为默认第 1 页，超过上限时钳制
        if (current < DEFAULT_CURRENT) {
            return DEFAULT_CURRENT;
        }
        return Math.min(current, MAX_CURRENT);
    }

    public static Long getSize() {
        long size = getPage().getSize();
        // size < 1 时钳制为默认值，size > 100 时钳制为 100
        if (size < 1L) {
            return Long.parseLong(DEFAULT_SIZE);
        }
        return Math.min(size, MAX_SIZE);
    }

    public static Long getLimitCurrent() {
        return (getCurrent() - 1) * getSize();
    }

    public static void remove() {
        PAGE_HOLDER.remove();
    }

}
