package com.aurora.controller;

import com.aurora.annotation.OptLog;
import com.aurora.model.dto.SystemConfigDTO;
import com.aurora.model.dto.WebsiteConfigDTO;
import com.aurora.model.vo.ResultVO;
import com.aurora.model.vo.SystemConfigVO;
import com.aurora.service.AuroraInfoService;
import com.aurora.service.SystemConfigProviderService;
import com.aurora.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.aurora.constant.OptTypeConstant.UPDATE;

@Api(tags = "系统配置模块")
@RestController
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemConfigProviderService configProvider;

    @Autowired
    private AuroraInfoService auroraInfoService;

    /**
     * 登录页公开只读配置：不走 /admin，默认匿名可访问。
     * 只返回展示所需的字段，不回传 OSS/MinIO 密钥等敏感配置。
     */
    @ApiOperation(value = "登录页公开配置")
    @GetMapping("/config/login")
    public ResultVO<Map<String, Object>> loginPageConfig() {
        Map<String, Object> result = new HashMap<>();
        String background = configProvider.getConfig("login.backgroundImage", "");
        result.put("loginBackgroundImage", isDisplayableImage(background) ? background : "");
        try {
            WebsiteConfigDTO websiteConfigDTO = auroraInfoService.getWebsiteConfig();
            if (websiteConfigDTO != null) {
                result.put("name", websiteConfigDTO.getName());
                result.put("logo", isDisplayableImage(websiteConfigDTO.getLogo()) ? websiteConfigDTO.getLogo() : "");
                result.put("favicon", websiteConfigDTO.getFavicon());
            }
        } catch (Exception ignored) {
            // 网站配置异常时登录页仍可用默认样式
        }
        return ResultVO.ok(result);
    }

    private boolean isDisplayableImage(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        String lower = url.trim().toLowerCase(Locale.ROOT);
        // .ico / 未知类型/掩码值不适合当作 CSS 背景
        return lower.contains(".png")
                || lower.contains(".jpg")
                || lower.contains(".jpeg")
                || lower.contains(".webp")
                || lower.contains(".gif")
                || lower.contains(".avif");
    }

    @ApiOperation(value = "获取系统配置")
    @GetMapping("/admin/system/config")
    public ResultVO<SystemConfigDTO> getSystemConfig() {
        return ResultVO.ok(systemConfigService.getSystemConfig());
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "更新系统配置")
    @PutMapping("/admin/system/config")
    public ResultVO<?> updateSystemConfig(@Valid @RequestBody SystemConfigVO systemConfigVO) {
        systemConfigService.updateSystemConfig(systemConfigVO);
        return ResultVO.ok();
    }

    @ApiOperation(value = "测试配置读取")
    @GetMapping("/admin/system/config/test")
    public ResultVO<Map<String, String>> testConfig() {
        Map<String, String> result = new HashMap<>();
        result.put("upload.mode", configProvider.getConfig("upload.mode", "未配置"));
        result.put("search.mode", configProvider.getConfig("search.mode", "未配置"));
        result.put("website.url", configProvider.getConfig("website.url", "未配置"));
        result.put("login.backgroundImage", configProvider.getConfig("login.backgroundImage", "未配置"));
        return ResultVO.ok(result);
    }

}
