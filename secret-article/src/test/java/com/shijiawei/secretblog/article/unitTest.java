package com.shijiawei.secretblog.article;

import com.shijiawei.secretblog.article.utils.CommonmarkUtils;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName: unitTest
 * Description:
 *
 * @Create 2025/10/27 上午 02:55
 */
public class unitTest {

    @Test
    @DisplayName("測試Markdown轉HTML")
    public void testMarkdownToHtml(){
        String html = CommonmarkUtils.parseMdToHTML("# 問題\n" +
                "今天天氣如何?日本\n" +
                "## 回答\n" +
                "抱歉，我無法提供即時的天氣資訊。建議您使用以下方式查詢日本各地的最新天氣：" +
                " - 前往日本氣象廳（Japan Meteorological Agency）官網或使用其手機應用程式" +
                " - 使用全球性的天氣服務，如 Weather.com、AccuWeather、OpenWeather 等" +
                " - 在手機上開啟天氣預報 App（如 Google 天氣、Apple 天氣）並輸入您想查詢的" +
                "城市或地區 這些渠道都能提供即時、精確的天氣預報與相關警報。祝您有個好天氣！");

        System.out.println(html);
    }

    @Test
    void TestIndex(){

        String likesCountPattern = RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern();//ams:article:comment:%s:comment_likes
        final String likesCountBucketName = String.format(likesCountPattern,1965494783750287361L);//ams:article:comment:1965494783750287361:comment_likes
        int i = likesCountPattern.indexOf("%s");
        System.out.println("i:"+i);//20
        String substring = likesCountBucketName.substring(i);//1965494783750287361:comment_likes
        int i2 = substring.indexOf(":");//19
        String finalStr = substring.substring(0,i2);


        System.out.println("substring:"+substring);
        System.out.println("finalStr:"+finalStr);


    }
    @Test
    void TestIndex2(){

        String likesCountPattern = RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern();//ams:article:comment:%s:comment_likes
        final String likesCountBucketName = String.format(likesCountPattern,1965494783750287361L);//ams:article:comment:1965494783750287361:comment_likes

        String[] parts = likesCountPattern.split("%s");
        String commentIdStr = likesCountBucketName.substring(parts[0].length(),
                likesCountBucketName.length() - parts[1].length());


    }

    @Test
    void TestIndexBest() {
        String likesCountPattern = RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern(); // ams:article:comment:%s:comment_likes
        String likesCountBucketName = String.format(likesCountPattern, 1965494783750287361L);

        // 1. 將 Pattern 中的 %s 替換成 Regex 的捕獲組 (.*?) 或 (\\d+)
        // 注意：這裡假設 Pattern 只有一個 %s，且其餘部分不含 Regex 特殊字元
        String regexPattern = likesCountPattern.replace("%s", "(\\d+)");//ams:article:comment:(\d+):comment_likes

        Pattern r = Pattern.compile(regexPattern);//
        Matcher m = r.matcher(likesCountBucketName);

        if (m.find()) {
            String id = m.group(1);
            System.out.println("Extracted ID: " + id);
        } else {
            System.out.println("Pattern not matched");
        }
    }
}
