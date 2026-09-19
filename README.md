# Aurora---前后端分离博客


## 前言
本项目基于https://github.com/linhaojun857/aurora编写

## 在线地址

- 前台链接：[www.gysy.ltd](https://www.gysy.ltd)
- 后台链接：[admin.gysy.ltd](https://admin.gysy.ltd)

## 效果图

### 图片1：
![图片1](https://github.com/jwt-coder/aurora/blob/main/%E6%95%88%E6%9E%9C%E5%9B%BE/1.png?raw=true)

### 图片2：
![图片2](https://github.com/jwt-coder/aurora/blob/main/%E6%95%88%E6%9E%9C%E5%9B%BE/2.png?raw=true)

![图片3](https://github.com/jwt-coder/aurora/blob/main/%E6%95%88%E6%9E%9C%E5%9B%BE/3.png?raw=true)

![图片4](https://github.com/jwt-coder/aurora/blob/main/%E6%95%88%E6%9E%9C%E5%9B%BE/4.png?raw=true)


## 相关技术

### 前端：

- 样式来自于：[hexo的aurora主题](https://github.com/auroral-ui/hexo-theme-aurora)
- 基础框架：vue3 + vite（前台 aurora-blog-vite / 后台 aurora-admin-v3）
- 状态管理：pinia（前台、后台均为 pinia，含 pinia-plugin-persistedstate）
- 路由组件：vue-router
- 网络请求：axios
- UI 组件：element-plus（前台登录/评论等）、naive-ui（后台）
- 前台文章渲染：markdown-it + katex/mermaid/emoji 等插件、prismjs 高亮
- 后台文章编辑：md-editor-v3
- 国际化：vue-i18n（前台中英文切换）
- 样式工具：tailwindcss（前台）、unocss + sass（后台）
- 后台图表：@antv/g2plot、echarts
- 其他技术：详见 `aurora-blog-vite` 与 `aurora-admin-v3` 的 package.json  
  （仓库内仍保留旧版 aurora-blog / aurora-admin，当前线上以 vite 版为主）

### 后端：

- 基础框架：springboot（2.3.7，JDK 8）
- ORM框架：mybatisplus
- 权限框架：springsecurity（JWT + 动态资源鉴权）
- 数据库：mysql
- 缓存中间件：redis
- 消息中间件：rabbitmq
- 搜索引擎：elasticsearch（可配 MySQL 搜索策略）
- 对象存储：minio（亦支持 OSS/本地上传）
- 定时任务：quartz
- 接口文档：knife4j / swagger
- 其他技术：详见后端项目的 pom.xml（hutool、lombok、ip2region、mail 等）

## 后续计划

- [ ] 前台代码优化
- [ ] 后台与前台文章预览渲染进一步对齐
- [ ] 点赞/收藏等互动数据可选落库策略与后台统计完善

## 部署

- 详见项目部署文档
- 前台、后台均为 Vite 构建后静态部署；后端 Spring Boot + MySQL + Redis + RabbitMQ（+ 可选 ES/MinIO）

## 交流群

- QQ群：338371628

## 鸣谢

- 感谢[jetbrains](https://www.jetbrains.com/)提供的开源开发许可证
