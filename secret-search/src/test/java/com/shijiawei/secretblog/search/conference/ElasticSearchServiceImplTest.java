package com.shijiawei.secretblog.search.conference;

import com.shijiawei.secretblog.common.dto.ArticlePreviewDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.search.feign.ArticleFeignClient;
import com.shijiawei.secretblog.search.service.impl.ElasticSearchServiceImpl;
import document.ArticlePreviewDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ElasticSearchServiceImpl 單元測試
 */
@ExtendWith(MockitoExtension.class)
class ElasticSearchServiceImplTest {

    @Mock
    private ArticleFeignClient articleFeignClient;

    @Mock
    private ElasticsearchOperations operations;

    @Mock
    private IndexOperations indexOperations;

    @InjectMocks
    private ElasticSearchServiceImpl elasticSearchService;

    @Test
    @DisplayName("createArticlePreviewDocByList - 批量建立文檔")
    void testCreateArticlePreviewDocByList_Success() {
       List<Long> articleIdList = Arrays.asList(1L,2L,3L);

        List<ArticlePreviewDTO> mockPreviews = createMockPreviews(articleIdList);
        R<List<ArticlePreviewDTO>> ok = R.ok(mockPreviews);
        when(articleFeignClient.getBatchArticlePreviewsForSearch(articleIdList)).thenReturn(ok);
        when(operations.indexOps(ArticlePreviewDocument.class)).thenReturn(indexOperations);

        elasticSearchService.createArticlePreviewDocByList(articleIdList);

        verify(articleFeignClient).getBatchArticlePreviewsForSearch(articleIdList);
        verify(operations).indexOps(ArticlePreviewDocument.class);
        verify(indexOperations).refresh();
    }

    /**
     * 建立模擬的 ArticlePreviewDTO 列表
     */
    private List<ArticlePreviewDTO> createMockPreviews(List<Long> articleIds) {
        List<ArticlePreviewDTO> previews = new ArrayList<>();
        for (Long id : articleIds) {
            previews.add(createMockPreview(id));
        }
        return previews;
    }

    /**
     * 建立單個模擬的 ArticlePreviewDTO
     */
    private ArticlePreviewDTO createMockPreview(Long articleId) {
        ArticlePreviewDTO preview = new ArticlePreviewDTO();
        preview.setArticleId(articleId);
        preview.setUserId(1L);
        preview.setNickName("TestUser");
        preview.setTitle("Test Article " + articleId);
        preview.setContent("<p>Test content for article " + articleId + "</p>");
        preview.setCategoryId(1L);
        preview.setCategoryName("TestCategory");
        preview.setAmsArtTagList(Collections.emptyList());
        preview.setCreateTime(LocalDateTime.now());
        preview.setUpdateTime(LocalDateTime.now());
        return preview;
    }
}
