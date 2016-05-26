package io.terminus.doctor.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dto.event.group.BaseGroupEvent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群事件表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorGroupEvent<T extends BaseGroupEvent> implements Serializable {
    private static final long serialVersionUID = 2651236908562482893L;

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

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
     * 猪群卡片id
     */
    private Long groupId;
    
    /**
     * 猪群号
     */
    private String groupCode;
    
    /**
     * 事件发生日期
     */
    private Date eventAt;
    
    /**
     * 事件类型 枚举 总共10种
     * @see io.terminus.doctor.event.enums.GroupEventType
     */
    private Integer type;
    
    /**
     * 事件名称 冗余枚举的name
     */
    private String name;
    
    /**
     * 事件描述
     */
    private String desc;
    
    /**
     * 事件发生猪舍id
     */
    private Long barnId;
    
    /**
     * 事件发生猪舍name
     */
    private String barnName;
    
    /**
     * 猪类枚举 9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;
    
    /**
     * 事件猪只数
     */
    private Integer quantity;
    
    /**
     * 总活体重(公斤)
     */
    private Double weight;
    
    /**
     * 平均体重(公斤)
     */
    private Double avgWeight;

    /**
     * 平均日龄
     */
    private Double avgDayAge;

    /**
     * 外部id
     */
    private String outId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 具体事件的内容通过json存储
     */
    @JsonIgnore
    private String extra;

    /**
     * 具体事件转换成的实体类
     * @see io.terminus.doctor.event.dto.event.group.BaseGroupEvent
     */
    @Setter(AccessLevel.NONE)
    private T extraMap;

    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人name
     */
    private String creatorName;

    @SneakyThrows
    public void setExtraMap(T extraMap){
        this.extraMap = extraMap;
        if(extraMap == null){
            this.extra = null;
        }else {
            this.extra = JSON_MAPPER.toJson(extraMap);
        }
    }
}