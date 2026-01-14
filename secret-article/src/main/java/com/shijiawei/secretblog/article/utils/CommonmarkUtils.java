package com.shijiawei.secretblog.article.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * ClassName: CommonmarkUtils
 * Description:
 *
 * @Create 2025/10/27 上午 02:48
 */
public class CommonmarkUtils {

    public static String parseMdToHTML(String md){
        //將Markdown轉成HTML後，再透過Jsoup進行白名單清洗，避免XSS
        Parser parser = Parser.builder().build();
        Node document = parser.parse(md);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);// "<p>This is <em>Markdown</em></p>\n"
        return sanitizeHtml(html);
    }

    /**
     * HTML白名單清洗(防止XSS)
     * @param html
     * @return
     */
    public static String sanitizeHtml(String html) {
        if (html == null) {
            return null;
        }

        Safelist safelist = Safelist.relaxed()
                //允許code/pre 的 class，避免語法高亮被清掉(若前端有用到)
                .addAttributes("code", "class")
                .addAttributes("pre", "class");

        Document.OutputSettings outputSettings = new Document.OutputSettings()
                //避免重新排版造成多餘空白或格式差異
                .prettyPrint(false);

        return Jsoup.clean(html, "", safelist, outputSettings);
    }

}
