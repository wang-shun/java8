package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 轮播图表Model类
 * author: 陈增辉
 * Date: 2016-05-17
 */
@Data
public class DoctorCarouselFigure implements Serializable {
    private static final long serialVersionUID = -783376977860027552L;

    private Long id;
    
    /**
     * 轮播图顺序, asc排序
     */
    private Integer index;
    
    /**
     * 状态: 1 启用, -1 不启用
     */
    private Integer status;
    
    /**
     * 轮播图链接地址
     */
    private String url;

    /**
     * 跳转地址
     */
    private String forward;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;

    public enum Status{
        ENABLED(1),
        DISABLED(-1);

        private int value;

        Status(int value){
            this.value = value;
        }

        public int value(){
            return this.value;
        }
    }
}
