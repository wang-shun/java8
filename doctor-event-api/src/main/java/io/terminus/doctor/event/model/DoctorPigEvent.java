package io.terminus.doctor.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.Params;
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
public class DoctorPigEvent implements Serializable {

    private static final long serialVersionUID = -6226648504842984629L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_EMPTY_MAPPER.getMapper();

    private Long id;    //事件Id

    private Long orgId; // 公司信息

    private String orgName;

    private Long farmId; // 猪场信息

    private String farmName;

    private Long pigId; // 猪Id

    private String pigCode; // 猪Code

    private Date eventAt;   // 创建时间

    private Integer type;   // 事件类型

    private Integer kind;   //类型

    private String name;    // 事件名称

    private String desc;    // 事件描述

    private Long barnId;    // 猪舍信息

    private String barnName;

    private Long relEventId;

    /**
     * 变动类型的id
     */
    private Long changeTypeId;

    /**
     * 销售单价(分)
     */
    private Long price;

    /**
     * 销售总额(分)
     */
    private Long amount;

    /**
     * 事件发生之前猪的状态
     *
     * @see io.terminus.doctor.event.enums.PigStatus
     */
    private Integer pigStatusBefore;

    /**
     * 事件发生之后猪的状态
     *
     * @see io.terminus.doctor.event.enums.PigStatus
     */
    private Integer pigStatusAfter;

    /**
     * 事件发生母猪的胎次 注意这个胎次是事件发生之前母猪的胎次
     */
    private Integer parity;

    /**
     * 是否可以进行受胎统计，就是妊娠检查阳性之后这个字段为true
     *
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    private Integer isImpregnation;

    /**
     * 是否可以进行分娩，就是分娩事件之后这个字段为true
     *
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    private Integer isDelivery;

    /**
     * 孕期，分娩时候统计
     */
    private Integer pregDays;

    /**
     * 哺乳天数，断奶事件发生统计
     */
    private Integer feedDays;

    /**
     * 妊娠检查结果，从extra中拆出来
     */
    private Integer pregCheckResult;

    /**
     * 断奶到配种的非生产天数
     */
    private Integer dpnpd = 0;

    /**
     * 配种到返情非生产天数
     */
    private Integer pfnpd = 0;

    /**
     * 配种到流产非生产天数
     */
    private Integer plnpd = 0;

    /**
     * 配种到死亡非生产天数
     */
    private Integer psnpd = 0;

    /**
     * 配种到阴性非生产天数
     */
    private Integer pynpd = 0;

    /**
     * 配种到淘汰非生产天数
     */
    private Integer ptnpd = 0;

    /**
     * 配种到配种非生产天数
     */
    private Integer jpnpd = 0;

    /**
     * 非生产天数 前面的总和
     */
    private Integer npd = 0;

    /**
     * 分娩总重(kg)
     */
    private Double farrowWeight;

    /**
     * 活仔数
     */
    private Integer liveCount;

    /**
     * 键仔数
     */
    private Integer healthCount;

    /**
     * 弱仔数
     */
    private Integer weakCount;

    /**
     * 木乃伊数
     */
    private Integer mnyCount;

    /**
     * 畸形数
     */
    private Integer jxCount;

    /**
     * 死胎数
     */
    private Integer deadCount;

    /**
     * 黑胎数
     */
    private Integer blackCount;

    /**
     * 断奶数
     */
    private Integer weanCount;

    /**
     * 断奶均重(kg)
     */
    private Double weanAvgWeight;

    /**
     * 当前配种次数
     */
    private Integer currentMatingCount;

    /**
     * 妊娠检查时间
     */
    private Date checkDate;

    /**
     * 配种时间
     */
    private Date mattingDate;

    /**
     * 分娩时间
     */
    private Date farrowingDate;

    /**
     * 流产时间
     */
    private Date abortionDate;

    /**
     * 断奶时间
     */
    private Date partweanDate;

    /**
     * 配种类型,这里的配种类型是为了统计增加 1: 后备到配种 2.流产到配种(妊娠检查) 3.流产到配种(流产事件) 4.断奶到配种 5.阴性到配种 6.返情到配种
     */
    private Integer doctorMateType;

    private String outId;

    @Setter(AccessLevel.NONE)
    private Map<String, Object> extraMap;

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extra;

    private String remark;

    /**
     * 事件操作人, 不一定是录入者
     */
    private Long operatorId;
    /**
     * 事件操作人, 不一定是录入者
     */
    private String operatorName;

    private Long creatorId;

    private String creatorName;

    private Long updatorId;

    private String updatorName;

    private Date createdAt;

    private Date updatedAt;

    @SneakyThrows
    public void setExtra(String extra) {
        this.extra = extra;
        if (Strings.isNullOrEmpty(extra)) {
            this.extraMap = Collections.emptyMap();
        } else {
            this.extraMap = OBJECT_MAPPER.readValue(extra, JacksonType.MAP_OF_OBJECT);
        }
    }

    @SneakyThrows
    public void setExtraMap(Map<String, Object> extraMap) {
        this.extraMap = Params.filterNullOrEmpty(extraMap);
        if (isNull(this.extraMap) || Iterables.isEmpty(this.extraMap.entrySet())) {
            this.extra = "";
        } else {
            this.extra = OBJECT_MAPPER.writeValueAsString(this.extraMap);
        }
    }
}