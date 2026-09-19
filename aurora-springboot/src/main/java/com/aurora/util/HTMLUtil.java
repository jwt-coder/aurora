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

    /**
     * 富文本消毒：去掉脚本类标签与事件属性，保留管理员排版用的常规 HTML。
     */
    public static String sanitizeRichText(String source) {
        if (source == null) {
            return null;
        }
        String result = source.replaceAll("(?i)<(script|style|iframe|object|embed|link|meta)[^>]*>[\\s\\S]*?</\\1>", "");
        result = result.replaceAll("(?i)<(script|style|iframe|object|embed|link|meta)[^>]*/?>", "");
        result = result.replaceAll("(?i)\\son\\w+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", "");
        result = result.replaceAll("(?i)javascript\\s*:", "");
        result = result.replaceAll("(?i)expression\\s*\\(", "");
        return result;
    }

    public static String deleteHMTLTag(String source) {
        source = source.replaceAll("&.{2,6}?;", "");
        source = source.replaceAll("<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>", "");
        source = source.replaceAll("<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>", "");
        return source;
    }

}
