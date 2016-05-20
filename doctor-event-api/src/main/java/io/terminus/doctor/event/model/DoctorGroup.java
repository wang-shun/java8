package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群卡片表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorGroup implements Serializable {
    private static final long serialVersionUID = -4667467895133336665L;

    private Long id;
    
    /**
     * 公司id
     */
    private Long orgId;
    
    /**
     * 公司名称
     */
    private String orgName;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 猪场名称
     */
    private String farmName;
    
    /**
     * 猪群号
     */
    private String groupCode;
    
    /**
     * 猪群批次号(雏鹰模式)
     */
    private String batchNo;
    
    /**
     * 建群时间
     */
    private Date openAt;
    
    /**
     * 关闭时间
     */
    private Date closeAt;
    
    /**
     * 枚举: 1:已建群, -1:已关闭
     */
    private Integer status;
    
    /**
     * 初始猪舍id
     */
    private Long initBarnId;
    
    /**
     * 初始猪舍name
     */
    private String initBarnName;
    
    /**
     * 当前猪舍id
     */
    private Long currentBarnId;
    
    /**
     * 当前猪舍名称
     */
    private String currentBarnName;
    
    /**
     * 猪类 枚举9种
     */
    private Integer pigType;
    
    /**
     * 性别 1:混合 2:母 3:公
     */
    private Integer sex;
    
    /**
     * 品种id
     */
    private Long breedId;
    
    /**
     * 品种name
     */
    private String breedName;
    
    /**
     * 品系id
     */
    private Long geneticId;
    
    /**
     * 品系name
     */
    private String geneticName;
    
    /**
     * 工作人员id
     */
    private Long staffId;
    
    /**
     * 工作人员name
     */
    private String staffName;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人name
     */
    private String creatorName;
    
    /**
     * 更新人id
     */
    private Long updatorId;
    
    /**
     * 更新人name
     */
    private String updatorName;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
