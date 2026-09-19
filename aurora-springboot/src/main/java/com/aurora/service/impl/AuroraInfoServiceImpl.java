package com.aurora.service.impl;

import com.alibaba.fastjson.JSON;
import com.aurora.model.dto.*;
import com.aurora.entity.*;
import com.aurora.mapper.*;
import com.aurora.service.AuroraInfoService;
import com.aurora.service.RedisService;
import com.aurora.service.UniqueViewService;
import com.aurora.util.BeanCopyUtil;
import com.aurora.util.IpUtil;
import com.aurora.model.vo.AboutVO;
import com.aurora.model.vo.WebsiteConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.aurora.constant.CommonConstant.*;
import static com.aurora.constant.RedisConstant.*;

@Service
public class AuroraInfoServiceImpl implements AuroraInfoService {

    @Autowired
    private WebsiteConfigMapper websiteConfigMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private TalkMapper talkMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private AboutMapper aboutMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UniqueViewService uniqueViewService;

    @Autowired
    private UniqueViewMapper uniqueViewMapper;

    @Autowired
    private VisitorAreaMapper visitorAreaMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void report() {
        String ipAddress = IpUtil.getIpAddress(request);
        UserAgent userAgent = IpUtil.getUserAgent(request);
        Browser browser = userAgent.getBrowser();
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        String uuid = ipAddress + browser.getName() + operatingSystem.getName();
        String md5 = DigestUtils.md5DigestAsHex(uuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // Redis Set 仅做"当天是否访问过"的去重（本来就是临时数据，每天由定时任务清空）；
        // 访问量与地区分布实时落库，数据库为唯一事实源，Redis 清空不会丢数据
        if (!redisService.sIsMember(UNIQUE_VISITOR, md5)) {
            String ipSource = IpUtil.getIpSource(ipAddress);
            String ipProvince = StringUtils.isNotBlank(ipSource) ? IpUtil.getIpProvince(ipSource) : UNKNOWN;
            recordDailyUniqueView();
            recordVisitorArea(ipProvince);
            redisService.sAdd(UNIQUE_VISITOR, md5);
        }
    }

    /**
     * 当日访客数实时累加到 t_unique_view（按天一行，不存在则新建）
     */
    private void recordDailyUniqueView() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        UniqueView todayView = uniqueViewMapper.selectOne(new LambdaQueryWrapper<UniqueView>()
                .ge(UniqueView::getCreateTime, dayStart)
                .lt(UniqueView::getCreateTime, dayStart.plusDays(1))
                .last("LIMIT 1"));
        if (Objects.isNull(todayView)) {
            try {
                uniqueViewMapper.insert(UniqueView.builder()
                        .viewsCount(1)
                        .createTime(LocalDateTime.now())
                        .build());
            } catch (DuplicateKeyException e) {
                // 并发新建撞车时忽略，下一访客会走累加分支
            }
        } else {
            uniqueViewMapper.update(null, new LambdaUpdateWrapper<UniqueView>()
                    .eq(UniqueView::getId, todayView.getId())
                    .setSql("views_count = views_count + 1"));
        }
    }

    /**
     * 访客省份分布实时累加到 t_visitor_area（按省份一行，不存在则新建）
     */
    private void recordVisitorArea(String province) {
        VisitorArea visitorArea = visitorAreaMapper.selectOne(new LambdaQueryWrapper<VisitorArea>()
                .eq(VisitorArea::getName, province)
                .last("LIMIT 1"));
        if (Objects.isNull(visitorArea)) {
            try {
                visitorAreaMapper.insert(VisitorArea.builder().name(province).value(1).build());
                return;
            } catch (DuplicateKeyException e) {
                // 并发新建撞车时退回累加分支
            }
        }
        visitorAreaMapper.update(null, new LambdaUpdateWrapper<VisitorArea>()
                .eq(VisitorArea::getName, province)
                .setSql("value = value + 1"));
    }

    /**
     * 博客总访问量：t_unique_view 全表累加（report 已实时落库，无需再依赖 Redis 计数器）
     */
    private Integer getBlogViewsCount() {
        Object total = uniqueViewMapper.selectObjs(new QueryWrapper<UniqueView>()
                        .select("IFNULL(SUM(views_count), 0)"))
                .stream().findFirst().orElse(0);
        return ((Number) total).intValue();
    }

    @SneakyThrows
    @Override
    public AuroraHomeInfoDTO getAuroraHomeInfo() {
        // count 条件与首页文章列表（listArticles）保持一致：is_delete = 0 且 status in (1, 2)
        Integer articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, FALSE)
                .in(Article::getStatus, 1, 2));
        Integer categoryCount = categoryMapper.selectCount(null);
        Integer tagCount = tagMapper.selectCount(null);
        Integer talkCount = talkMapper.selectCount(null);
        WebsiteConfigDTO websiteConfigDTO = getWebsiteConfig();
        return AuroraHomeInfoDTO.builder()
                .articleCount(articleCount)
                .categoryCount(categoryCount)
                .tagCount(tagCount)
                .talkCount(talkCount)
                .websiteConfigDTO(websiteConfigDTO)
                .viewCount(getBlogViewsCount()).build();
    }

    @Override
    public AuroraAdminInfoDTO getAuroraAdminInfo() {
        Integer viewsCount = getBlogViewsCount();
        Integer messageCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getType, 2));
        Integer userCount = userInfoMapper.selectCount(null);
        Integer articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsDelete, FALSE));
        List<UniqueViewDTO> uniqueViews = uniqueViewService.listUniqueViews();
        List<ArticleStatisticsDTO> articleStatisticsDTOs = articleMapper.listArticleStatistics();
        List<CategoryDTO> categoryDTOs = categoryMapper.listCategories();
        List<TagDTO> tagDTOs = BeanCopyUtil.copyList(tagMapper.selectList(null), TagDTO.class);
        // 访问量排行直接读 t_article.visit_count（数据库事实源），不再依赖 Redis zSet
        AuroraAdminInfoDTO auroraAdminInfoDTO = AuroraAdminInfoDTO.builder()
                .articleStatisticsDTOs(articleStatisticsDTOs)
                .tagDTOs(tagDTOs)
                .viewsCount(viewsCount)
                .messageCount(messageCount)
                .userCount(userCount)
                .articleCount(articleCount)
                .categoryDTOs(categoryDTOs)
                .uniqueViewDTOs(uniqueViews)
                .articleRankDTOs(listArticleRank())
                .build();
        return auroraAdminInfoDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWebsiteConfig(WebsiteConfigVO websiteConfigVO) {
        WebsiteConfig websiteConfig = WebsiteConfig.builder()
                .id(DEFAULT_CONFIG_ID)
                .config(JSON.toJSONString(websiteConfigVO))
                .build();
        websiteConfigMapper.updateById(websiteConfig);
        // 事务提交后再删除缓存，避免事务回滚后缓存被误删或读到旧数据
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisService.del(WEBSITE_CONFIG);
                }
            });
        } else {
            redisService.del(WEBSITE_CONFIG);
        }
    }

    @Override
    public WebsiteConfigDTO getWebsiteConfig() {
        WebsiteConfigDTO websiteConfigDTO;
        Object websiteConfig = redisService.get(WEBSITE_CONFIG);
        if (Objects.nonNull(websiteConfig)) {
            websiteConfigDTO = JSON.parseObject(websiteConfig.toString(), WebsiteConfigDTO.class);
        } else {
            String config = websiteConfigMapper.selectById(DEFAULT_CONFIG_ID).getConfig();
            websiteConfigDTO = JSON.parseObject(config, WebsiteConfigDTO.class);
            // 写入缓存并设置 24 小时过期，避免长期占用内存且能兜底自动刷新
            redisService.set(WEBSITE_CONFIG, config, CONFIG_CACHE_EXPIRE_TIME);
        }
        return websiteConfigDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAbout(AboutVO aboutVO) {
        About about = About.builder()
                .id(DEFAULT_ABOUT_ID)
                .content(JSON.toJSONString(aboutVO))
                .build();
        aboutMapper.updateById(about);
        // 事务提交后再删除缓存，避免事务回滚后缓存被误删或读到旧数据
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisService.del(ABOUT);
                }
            });
        } else {
            redisService.del(ABOUT);
        }
    }

    @Override
    public AboutDTO getAbout() {
        AboutDTO aboutDTO;
        Object about = redisService.get(ABOUT);
        if (Objects.nonNull(about)) {
            aboutDTO = JSON.parseObject(about.toString(), AboutDTO.class);
        } else {
            String content = aboutMapper.selectById(DEFAULT_ABOUT_ID).getContent();
            aboutDTO = JSON.parseObject(content, AboutDTO.class);
            // 写入缓存并设置 24 小时过期，避免长期占用内存且能兜底自动刷新
            redisService.set(ABOUT, content, CONFIG_CACHE_EXPIRE_TIME);
        }
        return aboutDTO;
    }

    private List<ArticleRankDTO> listArticleRank() {
        return articleMapper.selectList(new LambdaQueryWrapper<Article>()
                        .select(Article::getId, Article::getArticleTitle, Article::getVisitCount)
                        .eq(Article::getIsDelete, FALSE)
                        .gt(Article::getVisitCount, 0)
                        .orderByDesc(Article::getVisitCount)
                        .last("LIMIT 5"))
                .stream().map(article -> ArticleRankDTO.builder()
                        .articleTitle(article.getArticleTitle())
                        .viewsCount(article.getVisitCount())
                        .build())
                .collect(Collectors.toList());
    }

}
