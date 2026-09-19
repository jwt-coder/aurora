package com.aurora.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_visitor_area")
public class VisitorArea {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 省份名称
     */
    private String name;

    /**
     * 访客数量
     */
    private Integer value;

}
