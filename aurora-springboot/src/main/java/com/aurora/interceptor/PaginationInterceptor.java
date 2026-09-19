package com.aurora.interceptor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aurora.util.PageUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

import static com.aurora.constant.CommonConstant.*;

@Component
@SuppressWarnings("all")
public class PaginationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String currentPage = request.getParameter(CURRENT);
        String pageSize = Optional.ofNullable(request.getParameter(SIZE)).orElse(DEFAULT_SIZE);
        if (!Objects.isNull(currentPage) && !StringUtils.isEmpty(currentPage)) {
            long current;
            long size;
            try {
                // 先校验页码参数是否为数字，非数字时按默认第 1 页处理
                current = Long.parseLong(currentPage);
                size = Long.parseLong(pageSize);
            } catch (NumberFormatException e) {
                current = 1L;
                size = Long.parseLong(DEFAULT_SIZE);
            }
            PageUtil.setCurrentPage(new Page<>(current, size));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        PageUtil.remove();
    }

}