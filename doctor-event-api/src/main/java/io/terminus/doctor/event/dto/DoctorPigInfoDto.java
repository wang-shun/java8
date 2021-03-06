package io.terminus.doctor.event.dto;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 猪 分页列表数据信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigInfoDto implements Serializable{

    private static final long serialVersionUID = -3338994823651970751L;

    private Long id;

    private Long pigId;

    private Long orgId;
    private String orgName;

    private Long farmId;

    private String farmName;

    private Integer pigType;

    private String pigCode;

    private Integer status;

    private String statusName;

    private Integer dateAge;    // 日龄信息

    private Integer parity;     //  当前胎次/配种次数

    private Double weight;      // 当前体重

    private Date birthDay;

    private Date inFarmDate;

    private Long barnId;

    private String barnName;

    /**
     * 公猪类型
     * @see io.terminus.doctor.event.enums.BoarEntryType
     */
    private Integer boarType;

    /**
     * 猪舍类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer barnType;

    private String extraTrack;

    private Map<String,Object> extraTrackMap;

    private List<DoctorPigEvent> doctorPigEvents;

    private String extraTrackMessage;

    private Date updatedAt;

    private Long creatorId;
    private String creatorName;
    private Integer unweanQty;
    private Integer weanQty;

    /**
     * 物联网使用
     */
    private String rfid;

    public static DoctorPigInfoDto buildDoctorPigInfoDto(DoctorPig doctorPig, DoctorPigTrack doctorPigTrack, List<DoctorPigEvent> doctorPigEvents) {
        checkState(!isNull(doctorPig), "build.doctorPig.empty");
        List<Integer> kongHuaiPregCheckResultList = Lists.newArrayList(KongHuaiPregCheckResult.FANQING.getKey(), KongHuaiPregCheckResult.LIUCHAN.getKey(), KongHuaiPregCheckResult.YING.getKey());
        DoctorPigInfoDtoBuilder builder = DoctorPigInfoDto.builder()
                .id(doctorPig.getId()).pigId(doctorPig.getId()).orgId(doctorPig.getOrgId()).orgName(doctorPig.getOrgName()).farmId(doctorPig.getFarmId()).farmName(doctorPig.getFarmName())
                .pigType(doctorPig.getPigType()).pigCode(doctorPig.getPigCode()).birthDay(doctorPig.getBirthDate())
                .inFarmDate(doctorPig.getInFarmDate()).dateAge(Days.daysBetween(new DateTime(doctorPig.getBirthDate()), DateTime.now()).getDays())
                .creatorId(doctorPig.getCreatorId()).creatorName(doctorPig.getCreatorName()).rfid(doctorPig.getRfid()).boarType(doctorPig.getBoarType());

        if (!isNull(doctorPigTrack)) {
            PigStatus pigStatus = PigStatus.from(doctorPigTrack.getStatus());
            builder.status(doctorPigTrack.getStatus())
                    .statusName(pigStatus == null ? null : pigStatus.getName())
                    .parity(doctorPigTrack.getCurrentParity())
                    .weight(doctorPigTrack.getWeight())
                    .barnId(doctorPigTrack.getCurrentBarnId())
                    .barnName(doctorPigTrack.getCurrentBarnName())
                    .barnType(doctorPigTrack.getCurrentBarnType())
                    .extraTrack(doctorPigTrack.getExtra())
                    .extraTrackMap(doctorPigTrack.getExtraMap())
                    .extraTrackMessage(doctorPigTrack.getExtraMessage())
                    .updatedAt(doctorPigTrack.getUpdatedAt())
                    .weanQty(doctorPigTrack.getWeanQty())
                    .unweanQty(doctorPigTrack.getUnweanQty());
            if (kongHuaiPregCheckResultList.contains(doctorPigTrack.getStatus())) {
                builder.statusName(KongHuaiPregCheckResult.from(doctorPigTrack.getStatus()).getName());
            }
        }
        builder.doctorPigEvents(doctorPigEvents);

        return builder.build();
    }
}
