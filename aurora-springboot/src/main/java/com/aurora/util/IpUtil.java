package com.aurora.util;

import com.aurora.constant.CommonConstant;
import eu.bitwalker.useragentutils.UserAgent;

import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class IpUtil {

    private static DbSearcher searcher;

    private static Method method;

    public static String getIpAddress(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        // 只有当直连地址是回环/内网地址（即请求确实来自反向代理）时才信任 X-Real-IP / X-Forwarded-For 等请求头，
        // 防止外网客户端通过伪造代理头绕过 IP 统计/限流
        if (isInnerOrLoopback(remoteAddr)) {
            String ipAddress = request.getHeader("X-Real-IP");
            if (isBlankOrUnknown(ipAddress)) {
                // X-Forwarded-For 可能是逗号分隔的链路，取第一个非空且非 unknown 的值（即最初客户端 IP）
                String xff = request.getHeader("X-Forwarded-For");
                if (!isBlankOrUnknown(xff)) {
                    for (String ip : xff.split(",")) {
                        String candidate = ip.trim();
                        if (!isBlankOrUnknown(candidate)) {
                            ipAddress = candidate;
                            break;
                        }
                    }
                }
            }
            if (isBlankOrUnknown(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (isBlankOrUnknown(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (isBlankOrUnknown(ipAddress)) {
                ipAddress = request.getHeader("HTTP_CLIENT_IP");
            }
            if (isBlankOrUnknown(ipAddress)) {
                ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (!isBlankOrUnknown(ipAddress)) {
                return StringUtils.substringBefore(ipAddress, ",").trim();
            }
        }
        // 直连地址为公网 IP（或代理头均不可用）时，直接使用 getRemoteAddr()
        String ipAddress = remoteAddr;
        if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
            //根据网卡取本机配置的IP
            InetAddress inet = null;
            try {
                inet = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                // Unable to get local host
            }
            if (inet != null) {
                ipAddress = inet.getHostAddress();
            }
        }
        return ipAddress;
    }

    /**
     * 判断 IP 是否为回环/内网地址：127.、10.、172.16-31.、192.168. 以及 IPv6 回环 0:0:0:0:0:0:0:1
     */
    private static boolean isInnerOrLoopback(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        ip = ip.trim();
        if ("0:0:0:0:0:0:0:1".equals(ip) || ip.startsWith("127.")) {
            return true;
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                return second >= 16 && second <= 31;
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean isBlankOrUnknown(String ip) {
        return StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip.trim());
    }

    @PostConstruct
    private void initIp2regionResource() throws Exception {
        InputStream inputStream = new ClassPathResource("/ip/ip2region.db").getInputStream();
        byte[] dbBinStr = FileCopyUtils.copyToByteArray(inputStream);
        DbConfig dbConfig = new DbConfig();
        searcher = new DbSearcher(dbConfig, dbBinStr);
        method = searcher.getClass().getMethod("memorySearch", String.class);
    }

    public static String getIpSource(String ipAddress) {
        if (ipAddress == null || !Util.isIpAddress(ipAddress)) {
            return "";
        }
        try {
            DataBlock dataBlock = (DataBlock) method.invoke(searcher, ipAddress);
            String ipInfo = dataBlock.getRegion();
            if (!StringUtils.isEmpty(ipInfo)) {
                ipInfo = ipInfo.replace("|0", "");
                ipInfo = ipInfo.replace("0|", "");
                return ipInfo;
            }
        } catch (Exception e) {
            // Unable to get IP source
        }
        return "";
    }

    public static String getIpProvince(String ipSource) {
        if (StringUtils.isBlank(ipSource)) {
            return CommonConstant.UNKNOWN;
        }
        String[] strings = ipSource.split("\\|");
        if (strings.length > 1 && strings[1].endsWith("省")) {
            return StringUtils.substringBefore(strings[1], "省");
        }
        return strings[0];
    }

    public static UserAgent getUserAgent(HttpServletRequest request) {
        return UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
    }

}
