package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SEO 相關端點控制器
 * 提供 sitemap.xml 和 robots.txt 動態生成功能
 *
 * @Create 2025/12/7 下午11:27
 */
@RestController
public class SitemapController {

    @Autowired
    private AmsArticleService amsArticleService;

    // 從配置檔動態讀取域名
    @Value("${seo.site-url}")
    private String siteUrl;

    /**
     * 動態生成 sitemap.xml
     * 包含首頁和所有已發佈文章的連結
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateSitemap() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 首頁
        sb.append("  <url>\n");
        sb.append("    <loc>").append(siteUrl).append("/</loc>\n");
        sb.append("    <priority>1.0</priority>\n");
        sb.append("    <changefreq>daily</changefreq>\n");
        sb.append("  </url>\n");

        // 動態添加所有文章
        List<AmsArticle> articles = amsArticleService.getAllDistinctArticleIds();
        for (AmsArticle article : articles) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(siteUrl).append("/article/").append(article.getId()).append("</loc>\n");
            sb.append("    <changefreq>weekly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    /**
     * 動態生成 robots.txt
     */
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateRobotsTxt() {
        StringBuilder sb = new StringBuilder();
        sb.append("# robots.txt for SecretBlog\n");
        sb.append("User-agent: *\n");
        sb.append("Allow: /\n\n");
        sb.append("Disallow: /api/\n");
        sb.append("Disallow: /admin/\n\n");
        sb.append("Sitemap: ").append(siteUrl).append("/sitemap.xml\n");
        return sb.toString();
    }
}
