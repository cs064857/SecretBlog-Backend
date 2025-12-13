package com.shijiawei.secretblog.search.service;

import document.ArticlePreviewDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * ClassName: ElasticSearchService
 * Description: Elasticsearch 服務接口
 * 定義文章預覽文檔的建立與管理功能
 *
 * @Create 2025/12/10 下午5:20
 */
public interface ElasticSearchService {

    /**
     * 根據 articleId 建立文章預覽 Elasticsearch 文檔
     *
     * @param articleId 文章ID
     */
    void createArticlePreviewDoc(Long articleId);

    /**
     * 批量建立所有文章預覽 Elasticsearch 文檔
     * 用於 ElasticSearch 初始化時一次性建立所有 ArticlePreview 的 Doc
     */
    void createArticlePreviewAllListDoc();

    /**
     * 根據指定的 articleId 列表批量建立文章預覽 Elasticsearch 文檔
     *
     * @param articleIds 需要建立 ES 文檔的文章 ID 列表
     */
    void createArticlePreviewDocByList(List<Long> articleIds);

    /**
     * 確保指定的 Elasticsearch 索引存在
     * 若索引不存在，則創建索引並設定映射
     * 這是通用方法，可用於任何 Document 類型
     *
     * @param documentClass 文檔類型的 Class 對象
     * @param <T> 文檔類型泛型
     */
    <T> boolean ensureIndexExists(Class<T> documentClass);

    /**
     * 獲取ArticlePreview索引的文檔總數量
     */
    long getArticlePreviewIndexDocCount();

    /**
     * 檢查 ArticlePreview 索引是否已完整建立
     * 透過比較資料庫文章數量與 ES 索引文檔數量來判斷
     * 
     * @return true 表示索引完整（ES 文檔數 >= 資料庫文章數），false 表示索引不完整
     */
    boolean isArticlePreviewIndexComplete();

    /**
     * 執行高亮搜索
     * @param keyword 搜索關鍵字
     * @param pageable 分頁參數
     * @return 包含高亮內容的分頁結果
     */
//    Page<ArticlePreviewDocument> searchWithHighlight(String searchType,String keyword, Pageable pageable, String... fields);
    Page<ArticlePreviewDocument> searchWithHighlight(String keyword, Pageable pageable);

    /**
     * 執行高亮搜索（可選依分類過濾）
     *
     * @param keyword    搜索關鍵字
     * @param pageable   分頁參數
     * @param categoryId 文章分類 ID（可選）
     * @return 包含高亮內容的分頁結果
     */
    Page<ArticlePreviewDocument> searchWithHighlight(String keyword, Pageable pageable, Long categoryId);

    /**
     * 根據 articleId 刪除文章預覽 Elasticsearch 文檔
     *
     * @param articleId 文章ID
     */
    void deleteArticlePreviewDoc(Long articleId);
}
