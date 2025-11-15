package com.shijiawei.secretblog.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
@MapperScan("com.shijiawei.secretblog.article.mapper")
class SecretArticleApplicationTests {


    @Autowired
    private AmsCommentMapper amsCommentMapper;

    @Autowired
    private AmsArticleMapper amsArticleMapper;

    @Test
    void testGetArticlesByCategoryIdAndPage(){
//        Page<AmsArticlePreviewVo> page =
        Page<AmsArticlePreviewVo> page = new Page<>(0, 20);

        IPage<AmsArticlePreviewVo> articlesByCategoryIdAndPage = amsArticleMapper.getArticlesPreviewPage(page, 2L);
        System.out.println("articlesByCategoryIdAndPage:"+articlesByCategoryIdAndPage);
        List<AmsArticlePreviewVo> articlesByCategoryIdAndPageRecords = articlesByCategoryIdAndPage.getRecords();
        System.out.println("articlesByCategoryIdAndPageRecords:"+articlesByCategoryIdAndPageRecords);
    }



    @Test
    void testGetStaticCommentDetails() {
        // 準備測試數據 - 使用實際存在的文章ID
        Long articleId = 1965494783750287361L;

        // 執行查詢
        List<AmsArtCommentStaticVo> result = amsCommentMapper.getStaticCommentDetails(articleId);

        // 驗證結果
//        assertNotNull(result, "查詢結果不應為null");
        System.out.println("評論統計結果: " + result);

        // 根據實際業務邏輯添加更多斷言
        // 例如:
        // assertTrue(result.getCommentCount() >= 0, "評論數量應該大於等於0");
    }

//	@Autowired
//	private RedissonClient redissonClient;
//	@Autowired
//	private RedisTemplate<String, String> redisTemplate;
//	@Test
//	void RedissonClient() {
//		redissonClient.getLock("articleLock").lock();
//		RBucket<Object> article = redissonClient.getBucket("article");
//		article.delete();
//		article.set("Hello", Duration.ofSeconds(30));
//		redissonClient.getLock("articleLock").unlock();
//	}
//	@Test
//	void RedisTemplate() {
//		redisTemplate.opsForValue().set("hello", "world");
//	}
//	@Test
//	public void test(){
//		String str = "abcde"+"#categoryId"+"weqe"+"#page";
//		String[] split = Arrays.stream(str.split("(?=#)"))
//				.filter(s -> s.startsWith("#"))
//				.toArray(String[]::new);
////		for (String s : split) {
////			System.out.println(s);
////		}
//	}
//	@Test
//	public void test1(){
//		String prefix = "AmsArticles_category:#{categoryId}_#{routerPage}";
//
//		String[] split = Arrays.stream(prefix.split("(?=#)"))  // 以 # 進行分割
//				.map(s -> s.replaceAll("\\{(.*?)}.*", "$1")) // 提取 {} 中的文字，清空 } 後的字，保留 #
//				.filter(s -> s.startsWith("#"))                // 確保保留以 # 開頭的數組
//				.toArray(String[]::new);                       // 轉換成 String 陣列
//
//		String[] split2 = Arrays.stream(prefix.split("(?=#)"))  // 以 # 進行分割
//				.map(s -> s.replaceAll("(#\\{.*?})[^}]*", "$1"))
//				.filter(s -> s.startsWith("#"))                // 確保保留以 # 開頭的數組
//				.toArray(String[]::new);                       // 轉換成 String 陣列
//		System.out.println();
//	}
//	@Test
//	public void test2(){
//		Integer categoryId=8;
//		Integer routerPage=1;
//		String keyExpression= "AmsArticles:categoryId_#{#categoryId}:routePage_#{#routePage}"; //正確,使用ParserContext.TEMPLATE_EXPRESSION,未使用則失敗
//		//上述結論若使用ParserContext.TEMPLATE_EXPRESSION需要#{#變量}
//
////		String keyExpression= "'AmsArticles:categoryId_' + #categoryId +':routePage_' + #routePage";//正確,不使用ParserContext.TEMPLATE_EXPRESSION
//		//上述結論若不使用ParserContext.TEMPLATE_EXPRESSION需要#變量,並且使用連接符號,不需要{與}
//
//
//		EvaluationContext context=new StandardEvaluationContext();
//
//		context.setVariable("categoryId",categoryId);
//		context.setVariable("routePage",routerPage);
//		log.info("context:{}",context);
//		SpelExpressionParser parser = new SpelExpressionParser();
//		String key = parser.parseExpression(keyExpression,ParserContext.TEMPLATE_EXPRESSION).getValue(context, String.class);
//		log.info("key:{}",key);
//	}
}
