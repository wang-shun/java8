package io.terminus.doctor.event.dto.event.group.input;

import com.google.common.base.Joiner;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Desc: 猪群时间录入信息基类(公用字段)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@Data
public abstract class BaseGroupInput implements Serializable {
    private static final long serialVersionUID = 3142495945186975856L;

    /**
     * 事件发生时间 yyyy-MM-dd
     */
    @NotEmpty(message = "date.not.null")
    private String eventAt;

    /**
     * 事件类型
     */
    //@NotNull(message = "event.type.not.null")
    private Integer eventType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否是自动生成的事件(用于区分是触发事件还是手工录入事件) 0 不是, 1 是
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    @NotNull(message = "isAuto.not.null")
    private Integer isAuto;

    @NotNull(message = "creatorId.not.null")
    private Long creatorId;

    private String creatorName;

    /**
     * 自动生成的事件，记录下关联的猪群事件id
     */
    private Long relGroupEventId;

    /**
     * 母猪触发的事件，记录下关联的猪事件id
     */
    private Long relPigEventId;

    /**
     * 料肉比的饲料用量
     */
    private Double fcrFeed;

    /**
     * 是否是母猪事件触发
     */
    private boolean sowEvent;

    public final String generateEventDesc(){
        Map<String, String> descMap = this.descMap();
        if(descMap == null){
            return null;
        }
        String desc = Joiner.on("#").withKeyValueSeparator("：").join(descMap);
        if (Objects.equals(isAuto, IsOrNot.YES.getValue())) {
            return "【系统自动】" + desc;
        }else if (Objects.equals(isAuto, IsOrNot.NO.getValue())) {
            return "【手工录入】" + desc;
        }else{
            return desc;
        }
    }

    public abstract Map<String, String> descMap();

    public static BaseGroupInput generateBaseGroupInputFromTypeAndExtra(Map<String, Object> extra, GroupEventType eventType){
        BaseGroupInput res;
        switch (eventType) {
            case ANTIEPIDEMIC:
                res = BeanMapper.map(extra, DoctorAntiepidemicGroupInput.class);
                break;
            case CHANGE:
                res = BeanMapper.map(extra, DoctorChangeGroupInput.class);
                break;
            case CLOSE:
                res = BeanMapper.map(extra, DoctorCloseGroupInput.class);
                break;
            case DISEASE:
                res = BeanMapper.map(extra, DoctorDiseaseGroupInput.class);
                break;
            case LIVE_STOCK:
                res = BeanMapper.map(extra, DoctorLiveStockGroupInput.class);
                break;
            case MOVE_IN:
                res = BeanMapper.map(extra, DoctorMoveInGroupInput.class);
                break;
            case NEW:
                res = BeanMapper.map(extra, DoctorNewGroupInput.class);
                break;
            case TRANS_FARM:
                res = BeanMapper.map(extra, DoctorTransFarmGroupInput.class);
                break;
            case TRANS_GROUP:
                res = BeanMapper.map(extra, DoctorTransGroupInput.class);
                break;
            case TURN_SEED:
                res = BeanMapper.map(extra, DoctorTurnSeedGroupInput.class);
                break;
            default:
                throw new IllegalArgumentException("enum GroupEventType error");
        }
        return res;
    }
}
