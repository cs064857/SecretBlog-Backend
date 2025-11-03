package com.shijiawei.secretblog.article;

import com.shijiawei.secretblog.article.utils.CommonmarkUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

}
