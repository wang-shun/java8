package io.terminus.doctor.event.dto;

import com.google.common.base.Joiner;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
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
import java.util.Date;
import java.util.Map;
import java.util.Objects;

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

    private Long relGroupEventId;
    private Long relPigEventId;

    /**
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    private Integer isAuto;

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

    public String generateEventDescFromExtra(Map<String, Object> extra){
        if(eventType == null){
            return this.eventDesc;
        }
        PigEvent pigEvent = PigEvent.from(eventType);
        if(pigEvent == null){
            return this.eventDesc;
        }

        AbstractPigEventInputDto baseDto = transFromPigEventAndExtra(pigEvent, extra);
        Map<String, String> fieldMap = baseDto.descMap();
        if(Objects.equals(pigEvent, PigEvent.FOSTERS_BY)){
            // 被拼窝的母猪的描述中按理说应当带上"拼窝来源母猪", 但是 dto 中没有这个字段, 那就把"被拼窝母猪"这个字段去掉, 别让客户注意到...嘻嘻~~~
            fieldMap.remove("被拼窝母猪");
        }
        if(Objects.equals(pigEvent, PigEvent.FARROWING)){
            return "分娩";
        }
        if(baseDto.getOperatorName() != null){
            fieldMap.put("操作人", baseDto.getOperatorName());
        }
        return Joiner.on("#").withKeyValueSeparator("：").join(fieldMap);
    }

    public Date generateEventAtFromExtra(Map<String, Object> extra){
        if(eventType == null){
            return null;
        }
        PigEvent pigEvent = PigEvent.from(eventType);
        if(pigEvent == null){
            return null;
        }
        Date eventAt = transFromPigEventAndExtra(pigEvent, extra).eventAt();
        if(eventAt != null){
            Date now = new Date();
            if(DateUtil.inSameDate(eventAt, now)){
                // 如果处在今天, 则使用此刻瞬间
                return now;
            } else {
                // 如果不在今天, 则将时间置为0, 只保留日期
                return Dates.startOfDay(eventAt);
            }
        }
        return null;
    }
    
    public static AbstractPigEventInputDto transFromPigEventAndExtra(PigEvent pigEvent, Map<String, Object> extra){
        if(pigEvent == null){
            return null;
        }
        AbstractPigEventInputDto dto;
        switch (pigEvent) {
            case ENTRY:
                dto = BeanMapper.map(extra, DoctorFarmEntryDto.class);
                break;
            case CHG_FARM:
                dto = BeanMapper.map(extra, DoctorChgFarmDto.class);
                break;
            case CHG_LOCATION:
                dto = BeanMapper.map(extra, DoctorChgLocationDto.class);
                break;
            case TO_MATING:
                dto = BeanMapper.map(extra, DoctorChgLocationDto.class);
                break;
            case TO_PREG:
                dto = BeanMapper.map(extra, DoctorChgLocationDto.class);
                break;
            case TO_FARROWING:
                dto = BeanMapper.map(extra, DoctorChgLocationDto.class);
                break;
            case CONDITION:
                // 有 conditionBackWeight (背膘) 字段的是母猪体况事件
                if(extra.get("conditionBackWeight") != null){
                    dto = BeanMapper.map(extra, DoctorConditionDto.class);
                }else{
                    // 无 conditionBackWeight (背膘) 字段的是公猪体况事件
                    dto = BeanMapper.map(extra, DoctorBoarConditionDto.class);
                }
                break;
            case DISEASE:
                dto = BeanMapper.map(extra, DoctorDiseaseDto.class);
                break;
            case REMOVAL:
                dto = BeanMapper.map(extra, DoctorRemovalDto.class);
                break;
            case SEMEN:
                dto = BeanMapper.map(extra, DoctorSemenDto.class);
                break;
            case VACCINATION:
                dto = BeanMapper.map(extra, DoctorVaccinationDto.class);
                break;
            case FOSTERS:
                dto = BeanMapper.map(extra, DoctorFostersDto.class);
                break;
            case FOSTERS_BY:
                dto = BeanMapper.map(extra, DoctorFostersDto.class);
                break;
            case MATING:
                dto = BeanMapper.map(extra, DoctorMatingDto.class);
                break;
            case PREG_CHECK:
                dto = BeanMapper.map(extra, DoctorPregChkResultDto.class);
                break;
            case FARROWING:
                dto = BeanMapper.map(extra, DoctorFarrowingDto.class);
                break;
            case WEAN:
                dto = BeanMapper.map(extra, DoctorPartWeanDto.class);
                break;
            case PIGLETS_CHG:
                dto = BeanMapper.map(extra, DoctorPigletsChgDto.class);
                break;
            default:
                throw new IllegalArgumentException("enum PigEvent error");
        }
        return dto;
    }
}
