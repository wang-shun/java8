package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 公司表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Data
public class DoctorOrg implements Serializable {
    private static final long serialVersionUID = 2000937811766527864L;

    private Long id;
    
    /**
     * 公司名称
     */
    private String name;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 营业执照复印件图片地址
     */
    private String license;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
