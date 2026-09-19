package com.aurora.service.impl;

import com.aurora.entity.SystemConfig;
import com.aurora.mapper.SystemConfigMapper;
import com.aurora.model.dto.SystemConfigDTO;
import com.aurora.model.vo.SystemConfigVO;
import com.aurora.service.RedisService;
import com.aurora.service.SystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.aurora.constant.RedisConstant.CONFIG_CACHE_EXPIRE_TIME;
import static com.aurora.constant.RedisConstant.SYSTEM_CONFIG;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public SystemConfigDTO getSystemConfig() {
        Map<String, String> configMap = getConfigMap();

        return SystemConfigDTO.builder()
                .websiteUrl(configMap.get("website.url"))
                .websiteName(configMap.get("website.name"))
                .websiteMessage(configMap.get("website.message"))
                .friendLinkUrl(configMap.get("friendLink.url"))
                .friendLinkEmail(configMap.get("friendLink.email"))
                .searchMode(configMap.get("search.mode"))
                .uploadMode(configMap.get("upload.mode"))
                .uploadOssUrl(configMap.get("upload.oss.url"))
                .uploadOssEndpoint(configMap.get("upload.oss.endpoint"))
                .uploadOssAccessKeyId(maskSecret(configMap.get("upload.oss.accessKeyId")))
                .uploadOssAccessKeySecret(maskSecret(configMap.get("upload.oss.accessKeySecret")))
                .uploadOssBucketName(configMap.get("upload.oss.bucketName"))
                .uploadMinioUrl(configMap.get("upload.minio.url"))
                .uploadMinioEndpoint(configMap.get("upload.minio.endpoint"))
                .uploadMinioAccesskey(maskSecret(configMap.get("upload.minio.accesskey")))
                .uploadMinioSecretKey(maskSecret(configMap.get("upload.minio.secretKey")))
                .uploadMinioBucketName(configMap.get("upload.minio.bucketName"))
                .uploadLocalPath(configMap.get("upload.local.path"))
                .loginBackgroundImage(configMap.get("login.backgroundImage"))
                .build();
    }

    private static final String SECRET_MASK = "****";

    /** 密钥类配置对外只展示掩码，避免接口回显完整凭据 */
    private String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            return secret;
        }
        if (secret.length() <= 4 || secret.contains(SECRET_MASK)) {
            return SECRET_MASK;
        }
        return secret.substring(0, 2) + SECRET_MASK + secret.substring(secret.length() - 2);
    }

    private boolean isMaskedSecret(String value) {
        return value != null && value.contains(SECRET_MASK);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSystemConfig(SystemConfigVO systemConfigVO) {
        // 构建配置 Map，只包含非 null 的值
        Map<String, String> configMap = new HashMap<>();
        putIfNotNull(configMap, "website.url", systemConfigVO.getWebsiteUrl());
        putIfNotNull(configMap, "website.name", systemConfigVO.getWebsiteName());
        putIfNotNull(configMap, "website.message", systemConfigVO.getWebsiteMessage());
        putIfNotNull(configMap, "friendLink.url", systemConfigVO.getFriendLinkUrl());
        putIfNotNull(configMap, "friendLink.email", systemConfigVO.getFriendLinkEmail());
        putIfNotNull(configMap, "search.mode", systemConfigVO.getSearchMode());
        putIfNotNull(configMap, "upload.mode", systemConfigVO.getUploadMode());
        putIfNotNull(configMap, "upload.oss.url", systemConfigVO.getUploadOssUrl());
        putIfNotNull(configMap, "upload.oss.endpoint", systemConfigVO.getUploadOssEndpoint());
        // 掩码占位表示「未修改」，不要写回库覆盖真实密钥
        putIfNotMasked(configMap, "upload.oss.accessKeyId", systemConfigVO.getUploadOssAccessKeyId());
        putIfNotMasked(configMap, "upload.oss.accessKeySecret", systemConfigVO.getUploadOssAccessKeySecret());
        putIfNotNull(configMap, "upload.oss.bucketName", systemConfigVO.getUploadOssBucketName());
        putIfNotNull(configMap, "upload.minio.url", systemConfigVO.getUploadMinioUrl());
        putIfNotNull(configMap, "upload.minio.endpoint", systemConfigVO.getUploadMinioEndpoint());
        putIfNotMasked(configMap, "upload.minio.accesskey", systemConfigVO.getUploadMinioAccesskey());
        putIfNotMasked(configMap, "upload.minio.secretKey", systemConfigVO.getUploadMinioSecretKey());
        putIfNotNull(configMap, "upload.minio.bucketName", systemConfigVO.getUploadMinioBucketName());
        putIfNotNull(configMap, "upload.local.path", systemConfigVO.getUploadLocalPath());
        putIfNotNull(configMap, "login.backgroundImage", systemConfigVO.getLoginBackgroundImage());

        // 批量更新：一次查询所有配置，然后批量更新
        if (!configMap.isEmpty()) {
            List<String> keys = new ArrayList<>(configMap.keySet());
            List<SystemConfig> configs = systemConfigMapper.selectList(
                    new LambdaQueryWrapper<SystemConfig>().in(SystemConfig::getConfigKey, keys)
            );

            // 创建 key -> config 的映射
            Map<String, SystemConfig> configMapByKey = configs.stream()
                    .collect(Collectors.toMap(SystemConfig::getConfigKey, c -> c));

            // 收集需要更新的配置
            List<SystemConfig> configsToUpdate = new ArrayList<>();
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                SystemConfig config = configMapByKey.get(entry.getKey());
                if (config != null) {
                    config.setConfigValue(entry.getValue());
                    configsToUpdate.add(config);
                }
            }

            // 批量保存
            if (!configsToUpdate.isEmpty()) {
                updateBatchById(configsToUpdate);
            }
        }

        // 事务提交后再删除缓存，避免事务回滚后缓存被误删或读到旧数据
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisService.del(SYSTEM_CONFIG);
                }
            });
        } else {
            redisService.del(SYSTEM_CONFIG);
        }
    }

    private void putIfNotNull(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void putIfNotMasked(Map<String, String> map, String key, String value) {
        if (value != null && !isMaskedSecret(value)) {
            map.put(key, value);
        }
    }

    private Map<String, String> getConfigMap() {
        @SuppressWarnings("unchecked")
        Map<String, String> configMap = (Map<String, String>) redisService.get(SYSTEM_CONFIG);
        
        if (configMap == null) {
            configMap = new HashMap<>();
            List<SystemConfig> configList = systemConfigMapper.selectList(null);
            for (SystemConfig config : configList) {
                configMap.put(config.getConfigKey(), config.getConfigValue());
            }
            // 写入缓存并设置 24 小时过期，避免长期占用内存且能兜底自动刷新
            redisService.set(SYSTEM_CONFIG, configMap, CONFIG_CACHE_EXPIRE_TIME);
        }
        
        return configMap;
    }

}
