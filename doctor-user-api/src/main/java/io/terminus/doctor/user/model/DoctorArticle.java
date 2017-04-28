package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 文章
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/24
 */
@Data
public class DoctorArticle implements Serializable {

    private static final long serialVersionUID = -1919998813097741662L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 类目ID
     */
    private Integer categoryId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 状态
     * 0未发布
     * -1停用
     * 1发布
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}