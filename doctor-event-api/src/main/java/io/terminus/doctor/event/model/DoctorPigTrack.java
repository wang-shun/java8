package io.terminus.doctor.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
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
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class DoctorPigTrack implements Serializable {

    private static final long serialVersionUID = 6287905644724949716L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_EMPTY_MAPPER.getMapper();

    private Long id;

    private Long farmId;

    private Long pigId;

    /**
     * 猪类型信息表数据
     *
     * @see DoctorPig.PigSex
     */
    private Integer pigType;

    /**
     * @see io.terminus.doctor.event.enums.PigStatus
     */
    private Integer status;

    /**
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    private Integer isRemoval;

    private Long currentBarnId;

    private String currentBarnName;

    private Integer currentBarnType;

    private Double weight;

    /**
     * 哺乳猪群id(断奶后此id置为null)
     */
    private Long groupId;

    /**
     * 分娩仔猪数
     */
    private Integer farrowQty;

    /**
     * 未断奶数量
     */
    private Integer unweanQty;

    /**
     * 断奶数量
     */
    private Integer weanQty;

    /**
     * 分娩均重(kg)
     */
    private Double farrowAvgWeight;

    /**
     * 断奶均重(kg)
     */
    private Double weanAvgWeight;

    private Date outFarmDate;

    /**
     * 猪关联Id sow : {parity : "eventId1,eventId2,eventId3"}
     * boar "eventId1,eventId2,eventId3" 对应的关联事件列表Ids
     */
    private String relEventIds;

    /**
     * 猪的最新有效事件
     */
    private Long currentEventId;

    @Setter(AccessLevel.NONE)
    private Map<String, Object> extraMap;

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extra;

    // 用于猪只消息提醒
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extraMessage;
//    @Setter(AccessLevel.NONE)
//    private List<DoctorPigMessage> extraMessageList;

    private Integer currentParity;

    /**
     * 当前配种次数
     */
    private Integer currentMatingCount;

    private String remark;

    private Long creatorId;

    private String creatorName;

    private Long updatorId;

    private String updatorName;

    private Date createdAt;

    private Date updatedAt;

    @SneakyThrows
    public void setExtra(String extra) {
        this.extra = Strings.nullToEmpty(extra);
        if (Strings.isNullOrEmpty(extra)) {
            this.extraMap = Maps.newHashMap();
        } else {
            this.extraMap = OBJECT_MAPPER.readValue(extra, JacksonType.MAP_OF_OBJECT);
        }
    }

    @SneakyThrows
    public void setExtraMap(Map<String, Object> extraMap) {
        this.extraMap = extraMap;
        if (isNull(extraMap) || Iterables.isEmpty(extraMap.entrySet())) {
            this.extra = "";
        } else {
            this.extra = OBJECT_MAPPER.writeValueAsString(extraMap);
        }
    }

    @SneakyThrows
    public void addAllExtraMap(Map<String, Object> extra) {
        if (isNull(extra) || Iterables.isEmpty(extra.entrySet())) {
            return;
        }
        if (isNull(this.extraMap)) {
            this.extraMap = Maps.newHashMap();
        }
        this.extraMap.putAll(extra);
        this.extra = OBJECT_MAPPER.writeValueAsString(this.extraMap);
    }

//    @SneakyThrows
//    public void setExtraMessage(String extraMessage) {
//        this.extraMessage = extraMessage;
//        if (Strings.isNullOrEmpty(extraMessage)) {
//            this.extraMessageList = Lists.newArrayList();
//        } else {
//            this.extraMessageList = OBJECT_MAPPER.readValue(extraMessage, new TypeReference<List<DoctorPigMessage>>() {
//            });
//        }
//    }
//
//    @SneakyThrows
//    public void setExtraMessageList(List<DoctorPigMessage> extraMessageList) {
//        this.extraMessageList = extraMessageList;
//        if (isNull(extraMessageList) || Iterables.isEmpty(extraMessageList)) {
//            this.extraMessage = "";
//        } else {
//            this.extraMessage = OBJECT_MAPPER.writeValueAsString(extraMessageList);
//        }
//    }


//    /**
//     * 通过当前胎次信息添加猪 关联事件信息内容
//     *
//     * @param pigType
//     * @param relEventId
//     */
//    public void addPigEvent(Integer pigType, Long relEventId) {
//        if (Objects.equals(pigType, DoctorPig.PigSex.BOAR.getKey())) {
//            addBoarPigRelEvent(relEventId);
//        } else if (Objects.equals(pigType, DoctorPig.PigSex.SOW.getKey())) {
//            addSowPigRelEvent(relEventId);
//        } else {
//            throw new IllegalStateException("input.pigType.notFund");
//        }
//    }

    /**
     * 添加母猪关联胎次信息
     *
     * @param relEventId
     */
    @SneakyThrows
    private void addSowPigRelEvent(Long relEventId) {
        checkArgument(!isNull(currentParity), "input.parity.empty");
        checkArgument(!isNull(relEventId), "input.relEventId.empty");

        Map<String, String> relEventIdsMap = null;
        if (Strings.isNullOrEmpty(this.relEventIds)) {
            relEventIdsMap = Maps.newHashMap();
        } else {
            relEventIdsMap = OBJECT_MAPPER.readValue(this.relEventIds, JacksonType.MAP_OF_STRING);
        }

        if (relEventIdsMap.containsKey(currentParity.toString())) {
            relEventIdsMap.put(currentParity.toString(), relEventIdsMap.get(currentParity.toString()) + "," + relEventId.toString());
        } else {
            relEventIdsMap.put(currentParity.toString(), relEventId.toString());
        }
        this.relEventIds = OBJECT_MAPPER.writeValueAsString(relEventIdsMap);
    }

    private void addBoarPigRelEvent(Long relEventId) {
        checkArgument(!isNull(relEventId), "pigTrack.revEventIdInput.error");
        if (Strings.isNullOrEmpty(this.relEventIds)) {
            this.relEventIds = relEventId.toString();
        } else {
            this.relEventIds = this.relEventIds + "," + relEventId.toString();
        }
    }
}