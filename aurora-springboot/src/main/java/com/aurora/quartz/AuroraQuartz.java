package com.aurora.quartz;

import com.alibaba.fastjson.JSON;
import com.aurora.handler.FilterInvocationSecurityMetadataSourceImpl;
import com.aurora.model.dto.ArticleSearchDTO;
import com.aurora.model.dto.UserAreaDTO;
import com.aurora.entity.*;
import com.aurora.mapper.ElasticsearchMapper;
import com.aurora.mapper.UserAuthMapper;
import com.aurora.service.*;
import com.aurora.util.BeanCopyUtil;
import com.aurora.util.IpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.aurora.constant.CommonConstant.UNKNOWN;
import static com.aurora.constant.RedisConstant.*;

@Slf4j
@Component("auroraQuartz")
public class AuroraQuartz {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private JobLogService jobLogService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private RoleResourceService roleResourceService;

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ElasticsearchMapper elasticsearchMapper;

    @Autowired
    private SystemConfigProviderService configProvider;

    @Autowired
    private FilterInvocationSecurityMetadataSourceImpl filterInvocationSecurityMetadataSource;

    // "统计访问量"任务（auroraQuartz.saveUniqueView）已退役：访问量改为 report() 实时落库 t_unique_view，
    // 方法已删除。注意：部署本版本前需先执行升级 SQL 删除 t_job 中的注册（或在后台停用该任务），
    // 否则凌晨调度触发时会报"方法不存在"的错误日志（不影响其它功能）。

    public void clear() {
        // 每天凌晨清空访客去重集合，重新统计当天新访客；
        // 访客地区已实时落库 t_visitor_area，不再清空
        redisService.del(UNIQUE_VISITOR);
    }

    public void statisticalUserArea() {
        Map<String, Long> userAreaMap = userAuthMapper.selectList(new LambdaQueryWrapper<UserAuth>().select(UserAuth::getIpSource))
                .stream()
                .map(item -> {
                    if (Objects.nonNull(item) && StringUtils.isNotBlank(item.getIpSource())) {
                        return IpUtil.getIpProvince(item.getIpSource());
                    }
                    return UNKNOWN;
                })
                .collect(Collectors.groupingBy(item -> item, Collectors.counting()));
        List<UserAreaDTO> userAreaList = userAreaMap.entrySet().stream()
                .map(item -> UserAreaDTO.builder()
                        .name(item.getKey())
                        .value(item.getValue())
                        .build())
                .collect(Collectors.toList());
        redisService.set(USER_AREA, JSON.toJSONString(userAreaList));
    }

    public void baiduSeo() {
        String websiteUrl = configProvider.getConfig("website.url", "https://www.gysy.ltd");
        List<Integer> ids = articleService.list().stream().map(Article::getId).collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Host", "data.zz.baidu.com");
        headers.add("User-Agent", "curl/7.12.1");
        headers.add("Content-Length", "83");
        headers.add("Content-Type", "text/plain");
        ids.forEach(item -> {
            String url = websiteUrl + "/articles/" + item;
            HttpEntity<String> entity = new HttpEntity<>(url, headers);
            restTemplate.postForObject("https://www.baidu.com", entity, String.class);
        });
    }

    public void clearJobLogs() {
        jobLogService.cleanJobLogs();
    }

    public void importSwagger() {
        resourceService.importSwagger();
        List<Integer> resourceIds = resourceService.list().stream().map(Resource::getId).collect(Collectors.toList());
        List<RoleResource> roleResources = new ArrayList<>();
        for (Integer resourceId : resourceIds) {
            roleResources.add(RoleResource.builder()
                    .roleId(1)
                    .resourceId(resourceId)
                    .build());
        }
        roleResourceService.saveBatch(roleResources);
        // 资源-角色关系变更后清空权限元数据缓存，下次请求重新加载
        filterInvocationSecurityMetadataSource.clearDataSource();
    }

    public void importDataIntoES() {
        elasticsearchMapper.deleteAll();
        List<Article> articles = articleService.list();
        for (Article article : articles) {
            elasticsearchMapper.save(BeanCopyUtil.copyObject(article, ArticleSearchDTO.class));
        }
    }
}
