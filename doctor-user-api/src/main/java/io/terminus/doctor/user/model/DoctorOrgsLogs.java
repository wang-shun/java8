package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-06-13 19:41:03
 * Created by [ your name ]
 */
@Data
public class DoctorOrgsLogs implements Serializable {

    private static final long serialVersionUID = -8013923734004464569L;

    /**
     * 鑷涓婚敭
     */
    private Long id;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 调整前公司名称
     */
    private String orgFrotName;
    
    /**
     * 调整后公司名称
     */
    private String orgLaterName;
    
    /**
     * 当前用户id
     */
    private Long creatorId;
    
    /**
     * 当前用户名称
     */
    private String creatorName;
    
    /**
     * 当前用户id
     */
    private Long updatorId;
    
    /**
     * 当前用户名称
     */
    private String updatorName;
    
    /**
     * 鍒涘缓鏃堕棿
     */
    private Date createdAt;

    /**
     * 鏇存柊鏃堕棿
     */
    private Date updatedAt;

}