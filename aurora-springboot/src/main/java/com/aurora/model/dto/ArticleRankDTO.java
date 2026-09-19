package com.aurora.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleRankDTO {

    private String articleTitle;

    private Integer viewsCount;

    /**
     * 文章点赞数
     */
    private Integer likeCount;

    /**
     * 文章收藏数
     */
    private Integer collectCount;

}
