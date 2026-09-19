package com.aurora.service;

import com.aurora.model.dto.*;
import com.aurora.entity.Article;
import com.aurora.model.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface ArticleService extends IService<Article> {

    TopAndFeaturedArticlesDTO listTopAndFeaturedArticles();

    PageResultDTO<ArticleCardDTO> listArticles();

    PageResultDTO<ArticleCardDTO> listArticlesByCategoryId(Integer categoryId);

    ArticleDTO getArticleById(Integer articleId, HttpServletRequest request);

    void accessArticle(ArticlePasswordVO articlePasswordVO);

    PageResultDTO<ArticleCardDTO> listArticlesByTagId(Integer tagId);

    PageResultDTO<ArchiveDTO> listArchives();

    PageResultDTO<ArticleAdminDTO> listArticlesAdmin(ConditionVO conditionVO);

    void saveOrUpdateArticle(ArticleVO articleVO);

    void updateArticleTopAndFeatured(ArticleTopFeaturedVO articleTopFeaturedVO);

    void updateArticleDelete(DeleteVO deleteVO);

    void deleteArticles(List<Integer> articleIds);

    ArticleAdminViewDTO getArticleByIdAdmin(Integer articleId);

    List<String> exportArticles(List<Integer> articleIdList);

    List<ArticleSearchDTO> listArticlesBySearch(ConditionVO condition);

    /**
     * 点赞/取消点赞（登录按用户去重，游客按 IP 去重），返回最新点赞数与是否已赞
     */
    ArticleLikeDTO likeArticle(Integer articleId, HttpServletRequest request);

    /**
     * 收藏/取消收藏（需登录），返回收藏后的状态
     */
    Boolean collectArticle(Integer articleId);

    /**
     * 我的收藏文章列表（需登录）
     */
    List<ArticleCardDTO> listCollectedArticles();

}
