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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Data
public class DoctorMaterialInfo implements Serializable{

    private static final long serialVersionUID = -6818633941387063734L;

    public static final Long DEFAULT_COUNT = 1000000l;

    private static final Integer PERCENT_SCALE = 4; // percent 数据大小比例

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private Long id;

    private Long farmId;

    private String farmName;

    private Integer type;

    private String materialName;

    private String remark;

    private Long unitGroupId;

    private String unitGroupName;

    private Long unitId;

    private String unitName;

    private Long defaultConsumeCount;

    private Long price;

    @Setter(AccessLevel.NONE)
    private Map<String,String> extraMap;

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
    public void setExtraMap(Map<String,String> extraMap){
        this.extraMap = extraMap;
        if(isNull(extraMap) || Iterables.isEmpty(extraMap.entrySet())){
            this.extra = null;
        }else {
            this.extra = OBJECT_MAPPER.writeValueAsString(extraMap);
        }
    }

    /**
     * 对应的物料生产信息( 原料, 药品 配比信息 )
     */
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialProduce{

        private Long total;

        private List<MaterialProduceEntry> materialProduceEntries ; // 原料占比信息

        private List<MaterialProduceEntry> medicalProduceEntries; // 对应的药品比例信息

        // 计算合计数量
        public Long calculateTotalPercent(){
            this.total = 0l;
            if(!isNull(materialProduceEntries)){
                this.total += materialProduceEntries.stream().map(MaterialProduceEntry::getMaterialCount).reduce((a,b)->a+b).orElse(0l);
            }

            BigDecimal totalDecimal = BigDecimal.valueOf(total);
            if(!isNull(materialProduceEntries)){
                materialProduceEntries.forEach(m->{
                    m.setPercent(BigDecimal.valueOf(m.getMaterialCount() * 100).divide(totalDecimal, PERCENT_SCALE, BigDecimal.ROUND_CEILING).doubleValue());
                });
            }
            return this.total;
        }

        public Boolean calculatePercentByTotal(Long baseCount){
            if(!isNull(materialProduceEntries)){
                materialProduceEntries.forEach(m->{
                    m.setMaterialCount(BigDecimal.valueOf(m.getMaterialCount() * baseCount).divide(BigDecimal.valueOf(total), 0, BigDecimal.ROUND_UP).longValue());
                });
            }

            if(!isNull(medicalProduceEntries)){
                materialProduceEntries.forEach(m->{
                    m.setMaterialCount(BigDecimal.valueOf(m.getMaterialCount() * baseCount).divide(BigDecimal.valueOf(total), 0, BigDecimal.ROUND_UP).longValue());
                });
            }

            this.total = baseCount;
            return Boolean.TRUE;
        }
    }


    /**
     * 物料生产Entry
     */
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialProduceEntry{

        private Long materialId;

        private String materialName;

        private Long materialCount;

        private Double percent;
    }
}