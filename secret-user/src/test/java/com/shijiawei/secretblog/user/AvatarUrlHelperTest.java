package com.shijiawei.secretblog.user;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ClassName: AvatarUrlHelperTest
 * Description:
 *
 * @Create 2026/1/12 上午3:29
 */
public class AvatarUrlHelperTest{



    String minioDomain = "https://storage.passerby.me";

    /**
     * 將輸入的 avatar 轉為資料庫儲存用的相對路徑。
     * 規則：
     * 1、若輸入為 MinIO 完整 URL(host/port 與設定相符)，回傳去除網域後的相對路徑。
     * 2、若輸入為相對路徑，回傳去除前置 '/' 後的相對路徑。
     * 3、若輸入為其他網域的完整 URL，原樣回傳。
     *
     */
    @Test
    public void testToStoragePath() {
        String avatar = "https://storage.passerby.me/user-assets-pub/e260073d-0f70-4754-8d7a-2da596ca861a.png";
        String trimmed = avatar.trim();
        if (isHttpUrl(trimmed)) {
            URI avatarUri = tryParseUri(trimmed);
            URI domainUri = tryParseDomainUri(minioDomain);

            String path = StringUtils.defaultString(avatarUri.getPath(), "");
            String domainPathPrefix = StringUtils.defaultString(domainUri.getPath(), "");
            if (StringUtils.isNotBlank(domainPathPrefix)
                    && !"/".equals(domainPathPrefix)
                    && path.startsWith(domainPathPrefix)) {
                path = path.substring(domainPathPrefix.length());
            }
            String string = StringUtils.removeStart(path, "/");
            System.out.println("string:"+string);
        }

        System.out.println("trimmed:"+trimmed);

    }

    /**
     * 將資料庫中的 avatar(路徑或完整 URL)轉為對外輸出的完整 URL。
     *
     * 規則：
     * 1、若已是完整 URL，原樣回傳。
     * 2、若是相對路徑且有設定 MinIO 網域，回傳 domain + '/' + path。
     * 3、若未設定 MinIO 網域，回傳原始值。
     *
     */
    public String toPublicUrl(String avatar) {
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

        // 允許使用者在設定中省略 scheme(
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
