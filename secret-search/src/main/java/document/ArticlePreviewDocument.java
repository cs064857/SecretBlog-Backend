package document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.search.vo.AmsArtTagsVo;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: ArticlePreviewDocument
 * Description:
 *
 * @Create 2025/12/9 下午8:24
 */
@Data
@Builder
@Setting(shards = 1,replicas = 1)
@Document(indexName = "article_preview")
public class ArticlePreviewDocument {


    /**
     * Elasticsearch 文檔ID(不可為空)
     */
    @Id
    private String id;

    /**
     * 文章id(雪花算法,不可為空)
     */
    @Field(type = FieldType.Keyword)
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    private Long articleId;

    /**
     * 用戶id(雪花算法,不可為空)
     */
    @Field(type = FieldType.Keyword)
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    private Long userId;


     /**
     * 文章作者名稱(不可為空,最多64字符)
     */
    @Field(type = FieldType.Keyword)
    private String nickName;

    /**
     * 文章標題(不可為空,最多64字符)
     */
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 原始文章內容(不可為空)
     */
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 文章分類id(雪花算法,不可為空)
     */
    @Field(type = FieldType.Keyword)
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    private Long categoryId;


    /**
     * 文章分類名稱(不可為空,最多64字符)
     */
    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type =FieldType.Object)
    private List<AmsArtTagsVo> amsArtTagList;

    /**
     * 文章創建時間(不可為空)
     */
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

     /**
      * 文章更新時間(不可為空)
      */
     @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second)
     private LocalDateTime updateTime;

    //    /**
//     * 文章標籤id列表(可為空)
//     */
//    @Field(type = FieldType.Keyword)
//    @JsonFormat(shape= JsonFormat.Shape.STRING)
//    private List<Long> tagsId;
//
//    /**
//     * 文章標籤名稱列表(可為空)
//     */
//    @Field(type = FieldType.Keyword)
//    @JsonFormat(shape= JsonFormat.Shape.STRING)
//    private List<Long> tagsName;

}
