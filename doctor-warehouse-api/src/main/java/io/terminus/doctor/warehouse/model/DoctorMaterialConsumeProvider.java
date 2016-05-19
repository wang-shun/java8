package io.terminus.doctor.warehouse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Builder;

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

    private Long eventCount;

    private Long staffId;

    private String staffName;

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

    public static enum  EVENT_TYPE{
        CONSUMER(1, "消耗事件"),
        PROVIDER(2, "提供事件");

        @Getter
        private Integer value;

        @Getter
        private String desc;

        private EVENT_TYPE(Integer value, String desc){
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
    }
}