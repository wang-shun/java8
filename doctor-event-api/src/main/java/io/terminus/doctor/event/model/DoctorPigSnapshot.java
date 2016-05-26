package io.terminus.doctor.event.model;

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
public class DoctorPigSnapshot implements Serializable{

    private static final long serialVersionUID = -7819883927315891506L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private Long id;

    private Long orgId;

    private Long farmId;

    private Long pigId;

    private Long eventId;

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String pigInfo;

    @Setter(AccessLevel.NONE)
    private Map<String,Object> pigInfoMap;

    private Date createdAt;

    private Date updatedAt;

    @SneakyThrows
    public void setPigInfo(String pigInfo){
        this.pigInfo = pigInfo;
        if(Strings.isNullOrEmpty(pigInfo)){
            this.pigInfoMap = Collections.emptyMap();
        }else {
            this.pigInfoMap = OBJECT_MAPPER.readValue(pigInfo, JacksonType.MAP_OF_OBJECT);
        }
    }

    @SneakyThrows
    public void setPigInfoMap(Map<String,Object> pigInfoMap){
        this.pigInfoMap = pigInfoMap;
        if(isNull(pigInfoMap) || Iterables.isEmpty(pigInfoMap.entrySet())){
            this.pigInfo = null;
        }else {
            this.pigInfo = OBJECT_MAPPER.writeValueAsString(pigInfoMap);
        }
    }
}