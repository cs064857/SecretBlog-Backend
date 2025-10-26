package com.shijiawei.secretblog.article.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * ClassName: CommonmarkUtils
 * Description:
 *
 * @Create 2025/10/27 上午 02:48
 */
public class CommonmarkUtils {

    public static String parseMdToHTML(String md){
        Parser parser = Parser.builder().build();
        Node document = parser.parse(md);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);// "<p>This is <em>Markdown</em></p>\n"
        return html;
    }

}
