package com.aurora.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章点赞结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLikeDTO {

    /**
     * 点赞后的总数
     */
    private Integer likeCount;

    /**
     * 当前用户/游客是否已点赞
     */
    private Boolean isLiked;

}
