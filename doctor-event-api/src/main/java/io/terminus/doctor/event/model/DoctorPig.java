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
public class DoctorPig implements Serializable{

    private static final long serialVersionUID = -5981942073814626473L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private Long id;

    private Long orgId;

    private String orgName;

    private Long farmId;

    private String farmName;

    private String outId;

    private String pigCode;

    private Integer pigType;

    /**
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    private Integer isRemoval;  // 默认没有离场

    private String pigFatherCode;

    private String pigMotherCode;

    private Integer source;

    private Date birthDate;

    private Double birthWeight; // not include

    private Date inFarmDate;

    private Integer inFarmDayAge;

    private Long initBarnId;

    private String initBarnName;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    /**
     * 公猪类型
     * @see io.terminus.doctor.event.enums.BoarEntryType
     */
    private Integer boarType;

    @Setter(AccessLevel.NONE)
    private Map<String, Object> extraMap;

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extra;

    private String remark;

    private Long creatorId;

    private String creatorName;

    private Long updatorId;

    private String updatorName;

    private Date createdAt;

    private Date updatedAt;

    @SneakyThrows
    public void setExtraMap(Map<String,Object> extraMap){
        this.extraMap = extraMap;
        if(isNull(extraMap) || Iterables.isEmpty(extraMap.entrySet())){
            this.extra = "";
        }else {
            this.extra = OBJECT_MAPPER.writeValueAsString(extraMap);
        }
    }

    @SneakyThrows
    public void setExtra(String extra){
        this.extra = extra;
        if(Strings.isNullOrEmpty(extra)){
            this.extraMap = Collections.emptyMap();
        }else {
            this.extraMap = OBJECT_MAPPER.readValue(extra, JacksonType.MAP_OF_OBJECT);
        }
    }

    /**
     * 猪类型信息表数据
     */
    public enum PigSex {
        SOW(1, "母猪"),
        BOAR(2, "公猪");

        @Getter
        private Integer key;

        @Getter
        private String desc;

        PigSex(Integer key, String desc){
            this.key = key;
            this.desc = desc;
        }

        public static PigSex from(Integer key){
            for(PigSex pigSex : PigSex.values()){
                if(Objects.equals(pigSex.getKey(), key)){
                    return pigSex;
                }
            }
            return null;
        }
    }
}