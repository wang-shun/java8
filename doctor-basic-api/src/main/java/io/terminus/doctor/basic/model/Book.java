package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 20:38:36
 * Created by [ your name ]
 */
@Data
public class Book implements Serializable {

    private static final long serialVersionUID = 8344850660841148985L;

    /**
     * 鑷涓婚敭
     */
    private Long id;
    
    /**
     * 书名
     */
    private String bookName;
    
    /**
     * 作者
     */
    private String bookAuth;
    
    /**
     * 书的类型
     */
    private String bookType;
    
    /**
     * 单价
     */
    private Double bookPrice;
    
    /**
     * 书的发布时间
     */
    private Date bookTime;
    
    /**
     * 鍒涘缓鏃堕棿
     */
    private Date createdAt;

    /**
     * 鏇存柊鏃堕棿
     */
    private Date updatedAt;

}