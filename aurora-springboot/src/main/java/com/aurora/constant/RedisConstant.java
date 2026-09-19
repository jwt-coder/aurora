package com.aurora.constant;

public interface RedisConstant {

    long CODE_EXPIRE_TIME = 15 * 60;

    String USER_CODE_KEY = "code:";

    String BLOG_VIEWS_COUNT = "blog_views_count";

    String ARTICLE_VIEWS_COUNT = "article_views_count";

    String WEBSITE_CONFIG = "website_config";

    String USER_AREA = "user_area";

    String VISITOR_AREA = "visitor_area";

    String ABOUT = "about";

    String UNIQUE_VISITOR = "unique_visitor";

    String LOGIN_USER = "login_user";

    String ARTICLE_ACCESS = "article_access:";

    /**
     * 文章点赞去重：value 为用户id或 ip:xxx，member 为文章id（仅做去重，计数以 t_article.like_count 为准）
     */
    String ARTICLE_LIKE_USER = "article_like_user:";

    String SYSTEM_CONFIG = "system_config";

    /**
     * 配置类缓存（网站配置/关于我/系统配置）的过期时间：24 小时
     */
    long CONFIG_CACHE_EXPIRE_TIME = 24 * 60 * 60;

}
