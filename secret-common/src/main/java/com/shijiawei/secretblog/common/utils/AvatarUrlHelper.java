package com.shijiawei.secretblog.common.utils;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;

/**
 * Avatar URL/路徑轉換工具(靜態工具類)。
 * 設計目標：
 * 1、資料庫僅保存minio物件的相對路徑。
 * 2、對外輸出時，再以設定的minio網域拼接成完整URL。
 * 
 */
public final class AvatarUrlHelper {

    private AvatarUrlHelper() {
        // 工具類不允許被實例化
    }

    /**
     * 將輸入的 avatar 轉為資料庫儲存用的相對路徑。
     *
     * 規則：
     * 1、若輸入為minio完整URL(host/port與設定相符)，回傳去除網域後的相對路徑。
     * 2、若輸入為相對路徑，回傳去除前置 '/' 後的相對路徑。
     * 3、若輸入為其他網域的完整URL，原樣回傳。
     * 
     */
    public static String toStoragePath(String avatar, String minioDomain) {
        if (StringUtils.isBlank(avatar)) {
            return avatar;
        }

        String trimmed = avatar.trim();
        if (isHttpUrl(trimmed)) {
            URI avatarUri = tryParseUri(trimmed);
            URI domainUri = tryParseDomainUri(StringUtils.trimToEmpty(minioDomain));
            if (avatarUri == null || domainUri == null) {
                return trimmed;
            }
            if (!isSameHostAndPort(domainUri, avatarUri)) {
                return trimmed;
            }

            String path = StringUtils.defaultString(avatarUri.getPath(), "");
            String domainPathPrefix = StringUtils.defaultString(domainUri.getPath(), "");
            if (StringUtils.isNotBlank(domainPathPrefix)
                    && !"/".equals(domainPathPrefix)
                    && path.startsWith(domainPathPrefix)) {
                path = path.substring(domainPathPrefix.length());
            }
            return StringUtils.removeStart(path, "/");
        }

        return StringUtils.removeStart(trimmed, "/");
    }

    /**
     * 將資料庫中的 avatar(路徑或完整URL)轉為對外輸出的完整URL。
     * 規則：
     * 1、若已是完整UR，原樣回傳。
     * 2、若是相對路徑且有設定minio網域，回傳 domain + '/' + path。
     * 3、若未設定minio網域，回傳原始值。
     * 
     */
    public static String toPublicUrl(String avatar, String minioDomain) {
        if (StringUtils.isBlank(avatar)) {
            return avatar;
        }

        String trimmed = avatar.trim();
        if (isHttpUrl(trimmed)) {
            return trimmed;
        }

        if (StringUtils.isBlank(minioDomain)) {
            return trimmed;
        }

        String domain = minioDomain.trim();
        String path = StringUtils.removeStart(trimmed, "/");

        if (StringUtils.isBlank(path)) {
            return domain;
        }
        if (domain.endsWith("/")) {
            return domain + path;
        }
        return domain + "/" + path;
    }

    private static boolean isHttpUrl(String value) {
        return StringUtils.startsWithIgnoreCase(value, "http://")
                || StringUtils.startsWithIgnoreCase(value, "https://");
    }

    private static URI tryParseDomainUri(String domain) {
        if (StringUtils.isBlank(domain)) {
            return null;
        }
        URI uri = tryParseUri(domain.trim());
        if (uri != null && uri.getHost() != null) {
            return uri;
        }

        // 允許使用者在設定中省略scheme(例如 storage.example.com 或 storage.example.com:9000)
        String trimmed = domain.trim();
        if (!trimmed.contains("://")) {
            URI withScheme = tryParseUri("http://" + trimmed);
            if (withScheme != null && withScheme.getHost() != null) {
                return withScheme;
            }
        }
        return null;
    }

    private static URI tryParseUri(String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static boolean isSameHostAndPort(URI domainUri, URI targetUri) {
        if (domainUri == null || targetUri == null) {
            return false;
        }
        if (domainUri.getHost() == null || targetUri.getHost() == null) {
            return false;
        }
        if (!StringUtils.equalsIgnoreCase(domainUri.getHost(), targetUri.getHost())) {
            return false;
        }
        // 若 domain 有指定 port，則必須一致；未指定則不強制比對。
        return domainUri.getPort() == -1 || domainUri.getPort() == targetUri.getPort();
    }
}

