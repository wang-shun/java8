package io.terminus.doctor.event.dto;

import com.google.common.base.Joiner;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorAbortionDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.PigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Map;

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

    public String getEventDescFromExtra(Map<String, Object> extra){
        PigEvent pigEvent = PigEvent.from(eventType);
        if(pigEvent == null){
            return this.eventDesc;
        }

        Map<String, String> fieldMap;
        switch (pigEvent) {
            case ENTRY:
                fieldMap = BeanMapper.map(extra, DoctorFarmEntryDto.class).descMap();
                break;
            case CHG_FARM:
                fieldMap = BeanMapper.map(extra, DoctorChgFarmDto.class).descMap();
                break;
            case CHG_LOCATION:
                fieldMap = BeanMapper.map(extra, DoctorChgLocationDto.class).descMap();
                break;
            case TO_MATING:
                fieldMap = BeanMapper.map(extra, DoctorChgLocationDto.class).descMap();
                break;
            case TO_PREG:
                fieldMap = BeanMapper.map(extra, DoctorChgLocationDto.class).descMap();
                break;
            case TO_FARROWING:
                fieldMap = BeanMapper.map(extra, DoctorChgLocationDto.class).descMap();
                break;
            case CONDITION:
                fieldMap = BeanMapper.map(extra, DoctorConditionDto.class).descMap();
                break;
            case DISEASE:
                fieldMap = BeanMapper.map(extra, DoctorDiseaseDto.class).descMap();
                break;
            case REMOVAL:
                fieldMap = BeanMapper.map(extra, DoctorRemovalDto.class).descMap();
                break;
            case SEMEN:
                fieldMap = BeanMapper.map(extra, DoctorSemenDto.class).descMap();
                break;
            case VACCINATION:
                fieldMap = BeanMapper.map(extra, DoctorVaccinationDto.class).descMap();
                break;
            case FOSTERS:
                fieldMap = BeanMapper.map(extra, DoctorFostersDto.class).descMap();
                break;
            case FOSTERS_BY:
                fieldMap = BeanMapper.map(extra, DoctorFostersDto.class).descMap();
                // 被拼窝的母猪的描述中按理说应当带上"拼窝来源母猪", 但是 dto 中没有这个字段, 那就把"被拼窝母猪"这个字段去掉, 别让客户注意到...嘻嘻~~~
                fieldMap.remove("被拼窝母猪");
                break;
            case MATING:
                fieldMap = BeanMapper.map(extra, DoctorMatingDto.class).descMap();
                break;
            case PREG_CHECK:
                fieldMap = BeanMapper.map(extra, DoctorPregChkResultDto.class).descMap();
                break;
            case ABORTION:
                fieldMap = BeanMapper.map(extra, DoctorAbortionDto.class).descMap();
                break;
            case FARROWING:
                fieldMap = BeanMapper.map(extra, DoctorFarrowingDto.class).descMap();
                break;
            case WEAN:
                fieldMap = BeanMapper.map(extra, DoctorPartWeanDto.class).descMap();
                break;
            case PIGLETS_CHG:
                fieldMap = BeanMapper.map(extra, DoctorPigletsChgDto.class).descMap();
                break;
            default:
                return this.eventDesc;
        }
        return Joiner.on("#").withKeyValueSeparator("：").join(fieldMap);
    }
}
