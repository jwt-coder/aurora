-- 升级脚本：文章访问量 / 点赞 / 收藏 功能（2026-08-29）
-- 已有数据库执行本文件即可；全新部署直接使用 aurora初始化.sql，无需执行本文件。

-- 1. t_article 增加访问量、点赞数列（数据库为唯一事实源）
ALTER TABLE `t_article`
    ADD COLUMN `visit_count` int NOT NULL DEFAULT 0 COMMENT '访问量' AFTER `original_url`,
    ADD COLUMN `like_count`  int NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `visit_count`;

-- 2. 用户收藏表
CREATE TABLE IF NOT EXISTS `t_user_collect`  (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL COMMENT '用户id（t_user_auth主键，与登录token中的id一致）',
    `article_id` int NOT NULL COMMENT '文章id',
    `create_time` datetime NOT NULL COMMENT '收藏时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_article`(`user_id` ASC, `article_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- 3.（可选）把历史 Redis 中的文章访问量合并进库，执行一次后可删除 article_views_count 这个 zSet
--    Redis 命令（在 redis-cli 中执行，逐条合并后删除）：
--    ZRANGE article_views_count 0 -1 WITHSCORES  →  对每条执行下面的 SQL
--    UPDATE t_article SET visit_count = visit_count + <score> WHERE id = <articleId>;
--    DEL article_views_count

-- ============================================================
-- 第二部分：访客统计落库（总访问量 / 访客地区分布）
-- ============================================================

-- 4. 访客地区分布表（report 时实时累加，替代 Redis hash visitor_area）
CREATE TABLE IF NOT EXISTS `t_visitor_area`  (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份名称',
    `value` int NOT NULL DEFAULT 0 COMMENT '访客数量',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- 5. "统计访问量"定时任务已退役（访问量改为 report 时实时写入 t_unique_view），删除任务注册
DELETE FROM `t_job` WHERE `invoke_target` = 'auroraQuartz.saveUniqueView';

-- 6. 说明（无需执行 SQL）：
--    a. 总访问量改为 SUM(t_unique_view.views_count)，历史数据已在该表中，不会丢；
--    b. 访客地区历史数据原本每天被定时任务清空（Redis 中只有当天数据），无历史可迁，从升级起开始累积；
--    c. Redis 中的 blog_views_count 计数器已不再读取，可在 redis-cli 中 DEL blog_views_count 清理；
--    d. "清空redis访客记录"任务保留（每天清空去重 Set，重新统计当天新访客）。
