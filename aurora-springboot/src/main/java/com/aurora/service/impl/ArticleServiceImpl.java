package com.aurora.service.impl;

import com.alibaba.fastjson.JSON;
import com.aurora.model.dto.*;
import com.aurora.entity.Article;
import com.aurora.entity.ArticleTag;
import com.aurora.entity.Category;
import com.aurora.entity.Tag;
import com.aurora.entity.UserCollect;
import com.aurora.enums.FileExtEnum;
import com.aurora.enums.FilePathEnum;
import com.aurora.exception.BizException;
import com.aurora.mapper.ArticleMapper;
import com.aurora.mapper.ArticleTagMapper;
import com.aurora.mapper.CategoryMapper;
import com.aurora.mapper.TagMapper;
import com.aurora.mapper.UserCollectMapper;
import com.aurora.service.ArticleService;
import com.aurora.service.ArticleTagService;
import com.aurora.service.RedisService;
import com.aurora.service.SystemConfigProviderService;
import com.aurora.service.TagService;
import com.aurora.strategy.context.SearchStrategyContext;
import com.aurora.strategy.context.UploadStrategyContext;
import com.aurora.util.BeanCopyUtil;
import com.aurora.util.IpUtil;
import com.aurora.util.PageUtil;
import com.aurora.util.UserUtil;
import com.aurora.model.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import static com.aurora.constant.RabbitMQConstant.SUBSCRIBE_EXCHANGE;
import static com.aurora.constant.RedisConstant.*;
import static com.aurora.enums.ArticleStatusEnum.*;
import static com.aurora.enums.StatusCodeEnum.ARTICLE_ACCESS_FAIL;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleTagService articleTagService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @Autowired
    private SearchStrategyContext searchStrategyContext;

    @Autowired
    private SystemConfigProviderService configProvider;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @SneakyThrows
    @Override
    public TopAndFeaturedArticlesDTO listTopAndFeaturedArticles() {
        List<ArticleCardDTO> articleCardDTOs = articleMapper.listTopAndFeaturedArticles();
        if (articleCardDTOs.isEmpty()) {
            return new TopAndFeaturedArticlesDTO();
        } else if (articleCardDTOs.size() > 3) {
            articleCardDTOs = articleCardDTOs.subList(0, 3);
        }
        TopAndFeaturedArticlesDTO topAndFeaturedArticlesDTO = new TopAndFeaturedArticlesDTO();
        topAndFeaturedArticlesDTO.setTopArticle(articleCardDTOs.get(0));
        articleCardDTOs.remove(0);
        topAndFeaturedArticlesDTO.setFeaturedArticles(articleCardDTOs);
        return topAndFeaturedArticlesDTO;
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleCardDTO> listArticles() {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, 0)
                .in(Article::getStatus, 1, 2);
        Integer count = articleMapper.selectCount(queryWrapper);
        List<ArticleCardDTO> articles = articleMapper.listArticles(PageUtil.getLimitCurrent(), PageUtil.getSize());

        // 对加密文章进行内容过滤
        articles.forEach(article -> {
            if (article.getStatus() != null && article.getStatus().equals(2)) {
                article.setArticleContent("");
            }
        });

        return new PageResultDTO<>(articles, count);
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleCardDTO> listArticlesByCategoryId(Integer categoryId) {
        // count 条件与 getArticlesByCategoryId 的数据查询保持一致：is_delete = 0 且 status in (1, 2)
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, categoryId)
                .eq(Article::getIsDelete, 0)
                .in(Article::getStatus, 1, 2);
        Integer count = articleMapper.selectCount(queryWrapper);
        List<ArticleCardDTO> articles = articleMapper.getArticlesByCategoryId(PageUtil.getLimitCurrent(), PageUtil.getSize(), categoryId);

        // 对加密文章进行内容过滤
        articles.forEach(article -> {
            if (article.getStatus() != null && article.getStatus().equals(2)) {
                article.setArticleContent("");
            }
        });

        return new PageResultDTO<>(articles, count);
    }

    @SneakyThrows
    @Override
    public ArticleDTO getArticleById(Integer articleId, HttpServletRequest request) {
        Article articleForCheck = articleMapper.selectOne(new LambdaQueryWrapper<Article>().eq(Article::getId, articleId));
        if (Objects.isNull(articleForCheck)) {
            return null;
        }
        if (articleForCheck.getStatus().equals(2)) {
            Boolean isAccess;
            try {
                isAccess = redisService.sIsMember(ARTICLE_ACCESS + UserUtil.getUserDetailsDTO().getId(), articleId);
            } catch (Exception exception) {
                throw new BizException(ARTICLE_ACCESS_FAIL);
            }
            if (isAccess.equals(false)) {
                throw new BizException(ARTICLE_ACCESS_FAIL);
            }
        }
        // 访问量直接落库（原子自增），数据库为唯一事实源，避免仅存 Redis 重启丢失
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .setSql("visit_count = IFNULL(visit_count, 0) + 1"));
        ArticleDTO article = articleMapper.getArticleById(articleId);
        if (Objects.isNull(article)) {
            return null;
        }
        ArticleCardDTO preArticle = articleMapper.getPreArticleById(articleId);
        if (Objects.isNull(preArticle)) {
            preArticle = articleMapper.getLastArticle();
        }
        ArticleCardDTO nextArticle = articleMapper.getNextArticleById(articleId);
        if (Objects.isNull(nextArticle)) {
            nextArticle = articleMapper.getFirstArticle();
        }
        article.setPreArticleCard(preArticle);
        article.setNextArticleCard(nextArticle);

        // 点赞状态：登录按用户 id 去重，游客按 IP 去重
        article.setIsLiked(redisService.sIsMember(getLikeUserKey(request), articleId));
        // 收藏状态：仅登录用户有
        Integer loginUserId = getLoginUserIdOrNull();
        article.setIsCollected(Objects.nonNull(loginUserId) && userCollectMapper.selectCount(
                new LambdaQueryWrapper<UserCollect>()
                        .eq(UserCollect::getUserId, loginUserId)
                        .eq(UserCollect::getArticleId, articleId)) > 0);

        // 设置系统网站URL而不是用户个人网站
        if (article.getAuthor() != null) {
            String websiteUrl = configProvider.getConfig("website.url", "https://www.gysy.ltd");
            article.getAuthor().setWebsite(websiteUrl);
        }

        return article;
    }

    @Override
    public void accessArticle(ArticlePasswordVO articlePasswordVO) {
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>().eq(Article::getId, articlePasswordVO.getArticleId()));
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        if (article.getPassword().equals(articlePasswordVO.getArticlePassword())) {
            redisService.sAdd(ARTICLE_ACCESS + UserUtil.getUserDetailsDTO().getId(), articlePasswordVO.getArticleId());
        } else {
            throw new BizException("密码错误");
        }
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleCardDTO> listArticlesByTagId(Integer tagId) {
        // count 条件与 listArticlesByTagId 的数据查询保持一致：is_delete = 0 且 status in (1, 2)
        Integer count = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, 0)
                .in(Article::getStatus, 1, 2)
                .inSql(Article::getId, "SELECT article_id FROM t_article_tag WHERE tag_id = " + tagId));
        List<ArticleCardDTO> articles = articleMapper.listArticlesByTagId(PageUtil.getLimitCurrent(), PageUtil.getSize(), tagId);

        // 对加密文章进行内容过滤
        articles.forEach(article -> {
            if (article.getStatus() != null && article.getStatus().equals(2)) {
                article.setArticleContent("");
            }
        });

        return new PageResultDTO<>(articles, count);
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArchiveDTO> listArchives() {
        // count 条件与 listArchives 的数据查询保持一致：is_delete = 0 且 status in (1, 2)
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, 0)
                .in(Article::getStatus, 1, 2);
        Integer count = articleMapper.selectCount(queryWrapper);
        List<ArticleCardDTO> articles = articleMapper.listArchives(PageUtil.getLimitCurrent(), PageUtil.getSize());
        HashMap<String, List<ArticleCardDTO>> map = new HashMap<>();
        for (ArticleCardDTO article : articles) {
            LocalDateTime createTime = article.getCreateTime();
            int month = createTime.getMonth().getValue();
            int year = createTime.getYear();
            String key = year + "-" + month;
            if (Objects.isNull(map.get(key))) {
                List<ArticleCardDTO> articleCardDTOS = new ArrayList<>();
                articleCardDTOS.add(article);
                map.put(key, articleCardDTOS);
            } else {
                map.get(key).add(article);
            }
        }
        List<ArchiveDTO> archiveDTOs = new ArrayList<>();
        map.forEach((key, value) -> archiveDTOs.add(ArchiveDTO.builder().Time(key).articles(value).build()));
        archiveDTOs.sort((o1, o2) -> {
            String[] o1s = o1.getTime().split("-");
            String[] o2s = o2.getTime().split("-");
            int o1Year = Integer.parseInt(o1s[0]);
            int o1Month = Integer.parseInt(o1s[1]);
            int o2Year = Integer.parseInt(o2s[0]);
            int o2Month = Integer.parseInt(o2s[1]);
            if (o1Year > o2Year) {
                return -1;
            } else if (o1Year < o2Year) {
                return 1;
            } else return Integer.compare(o2Month, o1Month);
        });
        return new PageResultDTO<>(archiveDTOs, count);
    }

    @SneakyThrows
    @Override
    public PageResultDTO<ArticleAdminDTO> listArticlesAdmin(ConditionVO conditionVO) {
        // 访问量已切到 t_article.visit_count（数据库事实源），由 SQL 直接返回
        Integer count = articleMapper.countArticleAdmins(conditionVO);
        List<ArticleAdminDTO> articleAdminDTOs = articleMapper.listArticlesAdmin(PageUtil.getLimitCurrent(), PageUtil.getSize(), conditionVO);
        return new PageResultDTO<>(articleAdminDTOs, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateArticle(ArticleVO articleVO) {
        // 更新前先查旧状态，用于判断是否为"草稿→发布"转换
        Integer oldStatus = null;
        if (Objects.nonNull(articleVO.getId())) {
            Article oldArticle = articleMapper.selectById(articleVO.getId());
            oldStatus = Objects.nonNull(oldArticle) ? oldArticle.getStatus() : null;
        }
        Category category = saveArticleCategory(articleVO);
        Article article = BeanCopyUtil.copyObject(articleVO, Article.class);
        if (Objects.nonNull(category)) {
            article.setCategoryId(category.getId());
        }
        article.setUserId(UserUtil.getUserDetailsDTO().getUserInfoId());
        this.saveOrUpdate(article);
        saveArticleTag(articleVO, article.getId());
        boolean publishTransition = article.getStatus().equals(1) && !Integer.valueOf(1).equals(oldStatus);
        if (publishTransition) {
            Message message = new Message(JSON.toJSONBytes(article.getId()), new MessageProperties());
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                // 事务提交后再发送订阅消息，避免消费者读到未提交数据
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        rabbitTemplate.convertAndSend(SUBSCRIBE_EXCHANGE, "*", message);
                    }
                });
            } else {
                rabbitTemplate.convertAndSend(SUBSCRIBE_EXCHANGE, "*", message);
            }
        }
    }

    @Override
    public void updateArticleTopAndFeatured(ArticleTopFeaturedVO articleTopFeaturedVO) {
        Article article = Article.builder()
                .id(articleTopFeaturedVO.getId())
                .isTop(articleTopFeaturedVO.getIsTop())
                .isFeatured(articleTopFeaturedVO.getIsFeatured())
                .build();
        articleMapper.updateById(article);
    }

    @Override
    public void updateArticleDelete(DeleteVO deleteVO) {
        List<Article> articles = deleteVO.getIds().stream()
                .map(id -> Article.builder()
                        .id(id)
                        .isDelete(deleteVO.getIsDelete())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(articles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticles(List<Integer> articleIds) {
        // 获取这些文章关联的所有标签ID
        List<Integer> tagIds = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>()
                        .select(ArticleTag::getTagId)
                        .in(ArticleTag::getArticleId, articleIds))
                .stream()
                .map(ArticleTag::getTagId)
                .distinct()
                .collect(Collectors.toList());
        
        // 删除文章-标签关联关系
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .in(ArticleTag::getArticleId, articleIds));
        
        // 删除文章
        articleMapper.deleteBatchIds(articleIds);
        
        // 删除没有关联文章的空标签
        if (CollectionUtils.isNotEmpty(tagIds)) {
            List<Integer> emptyTagIds = tagIds.stream()
                    .filter(tagId -> {
                        Integer count = articleTagMapper.selectCount(new LambdaQueryWrapper<ArticleTag>()
                                .eq(ArticleTag::getTagId, tagId));
                        return count == 0;
                    })
                    .collect(Collectors.toList());
            
            if (CollectionUtils.isNotEmpty(emptyTagIds)) {
                tagMapper.deleteBatchIds(emptyTagIds);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleAdminViewDTO getArticleByIdAdmin(Integer articleId) {
        Article article = articleMapper.selectById(articleId);
        Category category = categoryMapper.selectById(article.getCategoryId());
        String categoryName = null;
        if (Objects.nonNull(category)) {
            categoryName = category.getCategoryName();
        }
        List<String> tagNames = tagMapper.listTagNamesByArticleId(articleId);
        ArticleAdminViewDTO articleAdminViewDTO = BeanCopyUtil.copyObject(article, ArticleAdminViewDTO.class);
        articleAdminViewDTO.setCategoryName(categoryName);
        articleAdminViewDTO.setTagNames(tagNames);
        return articleAdminViewDTO;
    }

    @Override
    public List<String> exportArticles(List<Integer> articleIds) {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .select(Article::getArticleTitle, Article::getArticleContent)
                .in(Article::getId, articleIds));
        List<String> urls = new ArrayList<>();
        for (Article article : articles) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(article.getArticleContent().getBytes(StandardCharsets.UTF_8))) {
                String url = uploadStrategyContext.executeUploadStrategy(article.getArticleTitle() + FileExtEnum.MD.getExtName(), inputStream, FilePathEnum.MD.getPath());
                urls.add(url);
            } catch (Exception e) {
                log.error("导出文章失败: {}", article.getArticleTitle(), e);
                throw new BizException("导出文章失败");
            }
        }
        return urls;
    }

    @Override
    public List<ArticleSearchDTO> listArticlesBySearch(ConditionVO condition) {
        return searchStrategyContext.executeSearchStrategy(condition.getKeywords());
    }

    @Override
    public ArticleLikeDTO likeArticle(Integer articleId, HttpServletRequest request) {
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getId, articleId)
                .eq(Article::getIsDelete, 0));
        if (Objects.isNull(article)) {
            throw new BizException("文章不存在");
        }
        String likeUserKey = getLikeUserKey(request);
        // 以 Set 操作的返回值判断本次是否产生变更，避免并发双击导致计数与去重集合漂移
        boolean changed;
        if (Boolean.TRUE.equals(redisService.sIsMember(likeUserKey, articleId))) {
            Long removed = redisService.sRemove(likeUserKey, articleId);
            changed = Objects.nonNull(removed) && removed > 0;
            if (changed) {
                articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, articleId)
                        .setSql("like_count = GREATEST(IFNULL(like_count, 1) - 1, 0)"));
            }
        } else {
            Long added = redisService.sAdd(likeUserKey, articleId);
            changed = Objects.nonNull(added) && added > 0;
            if (changed) {
                articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, articleId)
                        .setSql("like_count = IFNULL(like_count, 0) + 1"));
            }
        }
        boolean liked = Boolean.TRUE.equals(redisService.sIsMember(likeUserKey, articleId));
        Integer likeCount = articleMapper.selectById(articleId).getLikeCount();
        return ArticleLikeDTO.builder().likeCount(likeCount).isLiked(liked).build();
    }

    @Override
    public Boolean collectArticle(Integer articleId) {
        Integer userId = getLoginUserIdOrNull();
        if (Objects.isNull(userId)) {
            throw new BizException("请登录后收藏");
        }
        if (Objects.isNull(articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getId, articleId)
                .eq(Article::getIsDelete, 0)))) {
            throw new BizException("文章不存在");
        }
        UserCollect collect = userCollectMapper.selectOne(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getArticleId, articleId));
        if (Objects.nonNull(collect)) {
            userCollectMapper.deleteById(collect.getId());
            return false;
        }
        try {
            userCollectMapper.insert(UserCollect.builder()
                    .userId(userId)
                    .articleId(articleId)
                    .createTime(LocalDateTime.now())
                    .build());
        } catch (DuplicateKeyException e) {
            // 并发双击触发唯一键冲突：视为已收藏
            return true;
        }
        return true;
    }

    @Override
    public List<ArticleCardDTO> listCollectedArticles() {
        Integer userId = getLoginUserIdOrNull();
        if (Objects.isNull(userId)) {
            throw new BizException("请登录后查看收藏");
        }
        return articleMapper.listCollectedArticles(userId);
    }

    /**
     * 点赞去重 key：登录用户按 userId，游客按 IP
     */
    private String getLikeUserKey(HttpServletRequest request) {
        Integer userId = getLoginUserIdOrNull();
        if (Objects.nonNull(userId)) {
            return ARTICLE_LIKE_USER + userId;
        }
        return ARTICLE_LIKE_USER + "ip:" + IpUtil.getIpAddress(request);
    }

    private Integer getLoginUserIdOrNull() {
        try {
            return UserUtil.getUserDetailsDTO().getId();
        } catch (Exception e) {
            return null;
        }
    }

    private Category saveArticleCategory(ArticleVO articleVO) {
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, articleVO.getCategoryName()));
        if (Objects.isNull(category) && !articleVO.getStatus().equals(DRAFT.getStatus())) {
            category = Category.builder()
                    .categoryName(articleVO.getCategoryName())
                    .build();
            categoryMapper.insert(category);
        }
        return category;
    }

    // Note: This method has @Transactional but is called from within the same class (saveOrUpdateArticle)
    // Spring's @Transactional only works when called through a proxy, so self-invocation won't create a new transaction
    // This is acceptable here as the calling method already has @Transactional
    @Transactional(rollbackFor = Exception.class)
    public void saveArticleTag(ArticleVO articleVO, Integer articleId) {
        if (Objects.nonNull(articleVO.getId())) {
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                    .eq(ArticleTag::getArticleId, articleVO.getId()));
        }
        List<String> tagNames = articleVO.getTagNames();
        if (CollectionUtils.isNotEmpty(tagNames)) {
            List<Tag> existTags = tagService.list(new LambdaQueryWrapper<Tag>()
                    .in(Tag::getTagName, tagNames));
            List<String> existTagNames = existTags.stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());
            List<Integer> existTagIds = existTags.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList());
            tagNames.removeAll(existTagNames);
            if (CollectionUtils.isNotEmpty(tagNames)) {
                List<Tag> tags = tagNames.stream().map(item -> Tag.builder()
                                .tagName(item)
                                .build())
                        .collect(Collectors.toList());
                tagService.saveBatch(tags);
                List<Integer> tagIds = tags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());
                existTagIds.addAll(tagIds);
            }
            List<ArticleTag> articleTags = existTagIds.stream().map(item -> ArticleTag.builder()
                            .articleId(articleId)
                            .tagId(item)
                            .build())
                    .collect(Collectors.toList());
            articleTagService.saveBatch(articleTags);
        }
    }

}
