package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Article;
import org.example.springboot.mapper.ArticleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private FileService fileService;

    public Result<?> createArticle(Article article) {
        try {
            int result = articleMapper.insert(article);
            if (result > 0) {
                LOGGER.info("创建资讯成功，资讯ID：{}", article.getId());
                return Result.success(article);
            }
            return Result.error("-1", "创建资讯失败");
        } catch (Exception e) {
            LOGGER.error("创建资讯失败：{}", e.getMessage());
            return Result.error("-1", "创建资讯失败：" + e.getMessage());
        }
    }

    public Result<?> updateArticle(Long id, Article article) {
        try {
            Article oldArticle = articleMapper.selectById(id);
            if (oldArticle == null) {
                return Result.error("-1", "资讯不存在");
            }

            // 处理封面图片
            String oldImg = oldArticle.getCoverImage();
            String newImg = article.getCoverImage();
            if (oldImg != null && newImg != null && !oldImg.equals(newImg)) {
                fileService.fileRemove(oldImg);
            }

            article.setId(id);
            int result = articleMapper.updateById(article);
            if (result > 0) {
                LOGGER.info("更新资讯成功，资讯ID：{}", id);
                return Result.success(article);
            }
            return Result.error("-1", "更新资讯失败");
        } catch (Exception e) {
            LOGGER.error("更新资讯失败：{}", e.getMessage());
            return Result.error("-1", "更新资讯失败：" + e.getMessage());
        }
    }

    public Result<?> deleteArticle(Long id) {
        try {
            Article article = articleMapper.selectById(id);
            if (article != null && article.getCoverImage() != null) {
                fileService.fileRemove(article.getCoverImage());
            }

            int result = articleMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除资讯成功，资讯ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除资讯失败");
        } catch (Exception e) {
            LOGGER.error("删除资讯失败：{}", e.getMessage());
            return Result.error("-1", "删除资讯失败：" + e.getMessage());
        }
    }

    public Result<?> getArticleById(Long id) {
        try {
            Article article = articleMapper.selectById(id);
            if (article != null) {
                // 增加浏览量
                article.setViewCount(article.getViewCount() + 1);
                articleMapper.updateById(article);
                return Result.success(article);
            }
            return Result.error("-1", "未找到资讯");
        } catch (Exception e) {
            LOGGER.error("获取资讯失败：{}", e.getMessage());
            return Result.error("-1", "获取资讯失败：" + e.getMessage());
        }
    }

    public Page<Article> getArticlesByPage(String title, Integer status,
                                         Integer currentPage, Integer size) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        
        if (title != null && !title.trim().isEmpty()) {
            queryWrapper.like(Article::getTitle, title.trim());
        }
        if (status != null) {
            queryWrapper.eq(Article::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Article::getCreatedAt);

        Page<Article> page = new Page<>(currentPage, size);
        return articleMapper.selectPage(page, queryWrapper);
    }

    public Result<?> updateArticleStatus(Long id, Integer status) {
        try {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return Result.error("-1", "资讯不存在");
            }
            
            article.setStatus(status);
            int result = articleMapper.updateById(article);
            if (result > 0) {
                LOGGER.info("更新资讯状态成功，资讯ID：{}，新状态：{}", id, status);
                return Result.success();
            }
            return Result.error("-1", "更新资讯状态失败");
        } catch (Exception e) {
            LOGGER.error("更新资讯状态失败：{}", e.getMessage());
            return Result.error("-1", "更新资讯状态失败：" + e.getMessage());
        }
    }

    public Result<?> deleteBatch(List<Long> ids) {
        try {
            // 删除相关的封面图片
            List<Article> articles = articleMapper.selectBatchIds(ids);
            for (Article article : articles) {
                if (article.getCoverImage() != null) {
                    fileService.fileRemove(article.getCoverImage());
                }
            }

            int result = articleMapper.deleteBatchIds(ids);
            if (result > 0) {
                LOGGER.info("批量删除资讯成功，删除数量：{}", result);
                return Result.success();
            }
            return Result.error("-1", "批量删除资讯失败");
        } catch (Exception e) {
            LOGGER.error("批量删除资讯失败：{}", e.getMessage());
            return Result.error("-1", "批量删除资讯失败：" + e.getMessage());
        }
    }
} 