package com.aurora.util;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.web.util.HtmlUtils;

public class HTMLUtil {

    // temporary unused
    private static final SensitiveWordBs sensitiveWordBs = SensitiveWordBs.newInstance()
            .ignoreCase(true)
            .ignoreWidth(true)
            .ignoreNumStyle(true)
            .ignoreChineseStyle(true)
            .ignoreEnglishStyle(true)
            .ignoreRepeat(true)
            .enableNumCheck(false)
            .enableEmailCheck(false)
            .enableUrlCheck(false)
            .init();

    /**
     * 对评论内容做 HTML 转义，防止 XSS。
     * 保留换行符 \n，由前端负责将换行渲染为 <br>。
     */
    public static String filter(String source) {
        if (source == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(source);
    }

    public static String deleteHMTLTag(String source) {
        source = source.replaceAll("&.{2,6}?;", "");
        source = source.replaceAll("<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>", "");
        source = source.replaceAll("<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>", "");
        return source;
    }

}
