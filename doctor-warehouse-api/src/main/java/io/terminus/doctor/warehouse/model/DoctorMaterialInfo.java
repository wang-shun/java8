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

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialInfo implements Serializable{

    private static final long serialVersionUID = -6818633941387063734L;

    public static final Long DEFAULT_COUNT = 1000000l;

    public static final Integer SCALE = 10; // 默认精度大小

    private static final Integer PERCENT_SCALE = 4; // percent 数据大小比例

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private Long id;

    private Long farmId;

    private String farmName;

    /**
     * @see io.terminus.doctor.warehouse.enums.WareHouseType
     * 物料移动Basic, type = 1 均是可以生产的饲料信息
     */
    @Deprecated
    private Integer type;

    /**
     * @see io.terminus.doctor.warehouse.enums.IsOrNot
     * 默认是 可以生产的 canProduce = 1
     */
    @Deprecated
    private Integer canProduce; // 是否可以生产物料信息

    private String materialName;

    private String inputCode;

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
    public static class MaterialProduce implements Serializable{

        private static final long serialVersionUID = 633401329050233302L;

        private Long total; //生产物料总量信息

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
                    m.setMaterialCount(
                            BigDecimal.valueOf(baseCount)
                                    .divide(BigDecimal.valueOf(total), SCALE, BigDecimal.ROUND_UP)
                                    .multiply(BigDecimal.valueOf(m.getMaterialCount())).longValue());
                });
            }

            if(!isNull(medicalProduceEntries)){
                medicalProduceEntries.forEach(m -> {
                    m.setMaterialCount(BigDecimal.valueOf(baseCount)
                            .divide(BigDecimal.valueOf(total), SCALE, BigDecimal.ROUND_UP)
                            .multiply(BigDecimal.valueOf(m.getMaterialCount())).longValue());
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
    public static class MaterialProduceEntry implements Serializable{

        private static final long serialVersionUID = -5681942806422877951L;

        private Long materialId;    //原料Id

        private String materialName;    //  原料名称

        private Long materialCount; // 原料数量信息

        private Double percent; //原料配比信息
    }
}