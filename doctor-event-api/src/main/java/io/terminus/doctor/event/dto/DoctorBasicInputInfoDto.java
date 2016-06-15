package io.terminus.doctor.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 基本的用户录入信息内容(信息录入基本字段信息)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBasicInputInfoDto implements Serializable{

    private static final long serialVersionUID = 3753583575280390916L;

    // basic exist pig info（进厂事件信息，不用录入）
    private Long pigId;

    /**
     * 对应的母猪类型（公猪，母猪）
     * @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     */
    private Integer pigType;

    private String pigCode;

    private Long barnId;

    private String barnName;

    // usual farm info
    private Long farmId;

    private String farmName;

    private Long orgId;

    private String orgName;

    private Long staffId;

    private String staffName;

    /**
     * @see io.terminus.doctor.event.enums.PigEvent
     */
    private Integer eventType;

    private String eventName;

    private String eventDesc;

    private Long relEventId;

    /**
     * 相同宿舍, 不同pig构建
     * @param pigId
     * @param pigType
     * @param pigCode
     * @return
     */
    public DoctorBasicInputInfoDto buildSameBarnPigInfo(Long pigId, Integer pigType, String pigCode){
        return DoctorBasicInputInfoDto.builder()
                .pigId(pigId).pigType(pigType).pigCode(pigCode).barnId(this.barnId).barnName(this.barnName)
                .farmId(this.farmId).farmName(this.farmName).orgId(this.orgId).orgName(this.orgName).staffId(this.staffId).staffName(this.staffName)
                .eventType(this.eventType).eventName(this.eventName).eventDesc(this.eventDesc).relEventId(this.relEventId)
                .build();
    }
}
