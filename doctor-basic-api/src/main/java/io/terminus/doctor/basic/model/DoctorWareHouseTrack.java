package io.terminus.doctor.basic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static java.util.Objects.isNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWareHouseTrack implements Serializable{

    private static final long serialVersionUID = 3828362673175562707L;

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private Long wareHouseId;

    private Long farmId;

    private String farmName;

    private Long managerId;

    private String managerName;

    private String materialLotNumber;

    private Double lotNumber;

    private Integer isDefault;

    @Setter(AccessLevel.NONE)
    private Map<String,Object> extraMap;

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extra;

    private Date createdAt;

    private Date updatedAt;

    @SneakyThrows
    public void setExtra(String extra){
        this.extra = extra;
        if(Strings.isNullOrEmpty(extra)){
            this.extraMap = Maps.newHashMap();
        }else {
            this.extraMap = OBJECT_MAPPER.readValue(extra, JacksonType.MAP_OF_OBJECT);
        }
    }

    @SneakyThrows
    public void setExtraMap(Map<String,Object> extraMap){
        this.extraMap = extraMap;
        if(isNull(extraMap)){
            this.extra = null;
        }else {
            this.extra = OBJECT_MAPPER.writeValueAsString(extraMap);
        }
    }
}