package com.xyrfs.bean.pojo.redis.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PagePojo implements Serializable {

    private static final long serialVersionUID = -3548881362738874861L;

    // key
    private String url;
    // 分页项目
    private Page page;

}
