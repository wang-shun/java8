package io.terminus.doctor.basic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
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
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static java.util.Objects.isNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialInWareHouse implements Serializable{

    private static final long serialVersionUID = 1042669312946487127L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_EMPTY_MAPPER.getMapper();

    private Long id;

    private Long farmId;

    private String farmName;

    private Long wareHouseId;

    private String wareHouseName;

    private Long materialId;

    private String materialName;

    /**
     * @see io.terminus.doctor.common.enums.WareHouseType
     */
    private Integer type;

    private Double lotNumber;

    private String unitGroupName;

    private String unitName;

    @Setter(AccessLevel.NONE)
    private Map<String,Object> extraMap;

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
}