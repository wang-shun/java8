package io.terminus.doctor.warehouse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Builder;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialConsumeProvider implements Serializable{

    private static final long serialVersionUID = 6834365500624638371L;

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private Long id;

    private Integer type;

    private Long farmId;

    private String farmName;

    private Long wareHouseId;

    private String wareHouseName;

    private Long materialId;

    private String materialName;

    private Date eventTime;

    private Integer eventType;

    private Double eventCount;

    /**
     * 本次出库/入库的单价
     * 入库单价,由前台传入,直接保存即可
     * 出库单价,需要计算: 先入库的先出库, 然后对涉及到的每一次入库的单价作加权平均
     */
    private Long unitPrice;

    private Long staffId;

    private String staffName;

    /**
     * 领用物资的猪舍, 仅事件类型为领用(EVENT_TYPE.CONSUMER)时才会有值
     * 此字段原本存在于extra中, 但为了按猪舍统计数据, 只好拆出来
     */
    private Long barnId;
    private String barnName;

    @Setter(AccessLevel.NONE)
    private Map<String, Object> extraMap;

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extra;

    private Long creatorId;

    private String creatorName;

    private Long updatorId;

    private String updatorName;

    private Date createdAt;

    private Date updatedAt;

    @SneakyThrows
    public void setExtra(String extra){
        this.extra = extra;
        if(Strings.isNullOrEmpty(extra)){
            this.extraMap = Collections.emptyMap();
        }else {
            this.extraMap = OBJECT_MAPPER.readValue(extra, JacksonType.MAP_OF_OBJECT);
        }
    }

    @SneakyThrows
    public void setExtraMap(Map<String,Object> extraMap){
        this.extraMap = extraMap;
        if(isNull(extraMap) || Iterables.isEmpty(extraMap.entrySet())){
            this.extra = null;
        }else {
            this.extra = OBJECT_MAPPER.writeValueAsString(extraMap);
        }
    }

    public enum  EVENT_TYPE{
        CONSUMER(1, "领用"), // 出库
        PROVIDER(2, "添加"), // 入库

        PANKUI(3, "盘亏"), // 出库
        PANYING(4, "盘盈"), // 入库

        DIAOCHU(5, "调出"), // 出库
        DIAORU(6, "调入"); // 入库

        @Getter
        private Integer value;

        @Getter
        private String desc;

        EVENT_TYPE(Integer value, String desc){
            this.value = value;
            this.desc = desc;
        }

        public static EVENT_TYPE from(Integer value){
            for (EVENT_TYPE event_type : EVENT_TYPE.values()){
                if(Objects.equals(event_type.getValue(), value)){
                    return event_type;
                }
            }
            return null;
        }

        /**
         * 是否为出库枚举
         * @return
         */
        public boolean isOut(){
            return CONSUMER == this || PANKUI == this || DIAOCHU == this;
        }

        /**
         * 是否为入库枚举
         * @return
         */
        public boolean isIn(){
            return PROVIDER == this || PANYING == this || DIAORU == this;
        }
    }

    public static DoctorMaterialConsumeProvider buildFromDto(DoctorMaterialConsumeProviderDto dto){
        return DoctorMaterialConsumeProvider.builder()
                .type(dto.getType())
                .farmId(dto.getFarmId()).farmName(dto.getFarmName())
                .wareHouseId(dto.getWareHouseId()).wareHouseName(dto.getWareHouseName())
                .materialId(dto.getMaterialTypeId()).materialName(dto.getMaterialName())
                .eventType(dto.getActionType()).eventTime(DateTime.now().toDate()).eventCount(dto.getCount()).unitPrice(dto.getUnitPrice())
                .staffId(dto.getStaffId()).staffName(dto.getStaffName())
                .creatorId(dto.getStaffId()).creatorName(dto.getStaffName())
                .build();

    }
}