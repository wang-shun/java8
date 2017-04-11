package io.terminus.doctor.event.handler.sow;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Component
public class DoctorSowPregCheckHandler extends DoctorAbstractEventHandler {

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        expectTrue(Objects.equals(fromTrack.getStatus(), PigStatus.Mate.getKey())
                        || Objects.equals(fromTrack.getStatus(), PigStatus.KongHuai.getKey())
                        || Objects.equals(fromTrack.getStatus(), PigStatus.Pregnancy.getKey())
                        || Objects.equals(fromTrack.getStatus(), PigStatus.Farrow.getKey())
                ,"pig.status.failed", PigEvent.from(executeEvent.getType()).getName(), PigStatus.from(fromTrack.getStatus()).getName());
        DoctorPregChkResultDto pregChkResultDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorPregChkResultDto.class);
        if (Objects.equals(executeEvent.getPregCheckResult(), PregCheckResult.LIUCHAN.getKey())) {
            expectTrue(notNull(pregChkResultDto.getAbortionReasonId()), "liuchan.reason.not.null", pregChkResultDto.getPigCode());
        }
        if (Objects.equals(executeEvent.getIsModify(), IsOrNot.NO.getValue())) {
            checkCanPregCheckResult(fromTrack.getStatus(), pregChkResultDto.getCheckResult(), pregChkResultDto.getPigCode());
        }
    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorPregChkResultDto pregChkResultDto = (DoctorPregChkResultDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pregChkResultDto.getPigId());
        expectTrue(notNull(doctorPigTrack), "pig.track.not.null", inputDto.getPigId());
        //妊娠检查结果，从extra中拆出来
        Integer pregCheckResult = pregChkResultDto.getCheckResult();
        doctorPigEvent.setPregCheckResult(pregCheckResult);

        //妊娠检查事件时间
        DateTime checkDate = new DateTime(pregChkResultDto.eventAt());
        doctorPigEvent.setCheckDate(checkDate.toDate());

        //查找最近一次配种事件
        DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        expectTrue(notNull(lastMate), "preg.last.mate.not.null", inputDto.getPigId());
        if (!Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())) {
            DateTime mattingDate = new DateTime(lastMate.getEventAt());
            int npd = Math.abs(Days.daysBetween(checkDate, mattingDate).getDays());

            if (Objects.equals(pregCheckResult, PregCheckResult.FANQING.getKey())) {
                //返情对应的pfNPD
                doctorPigEvent.setPfnpd(doctorPigEvent.getPfnpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            } else if (Objects.equals(pregCheckResult, PregCheckResult.YING.getKey())) {
                //阴性对应的pyNPD
                doctorPigEvent.setPynpd(doctorPigEvent.getPynpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            } else if (Objects.equals(pregCheckResult, PregCheckResult.LIUCHAN.getKey())) {
                //流产对应的plNPD
                doctorPigEvent.setBasicId(pregChkResultDto.getAbortionReasonId());
                doctorPigEvent.setBasicName(pregChkResultDto.getAbortionReasonName());
                doctorPigEvent.setPlnpd(doctorPigEvent.getPlnpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            }

        }

        //根据猪类判断, 如果是逆向: 空怀 => 阳性, 需要删掉以前的空怀事件
        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.KongHuai.getKey())) {
            DoctorPigEvent lastPregEvent = doctorPigEventDao.queryLastPregCheck(doctorPigTrack.getPigId());
            expectTrue(notNull(lastPregEvent), "preg.check.event.not.null", pregChkResultDto.getPigId());
            expectTrue(PregCheckResult.KONGHUAI_RESULTS.contains(lastPregEvent.getPregCheckResult()), "preg.check.result.error", lastPregEvent.getPregCheckResult(), pregChkResultDto.getPigCode());
            log.info("remove old preg check event info:{}", lastPregEvent);

            doctorPigEvent.setId(lastPregEvent.getId());    //把id放进去, 用于更新数据
            doctorPigEvent.setRelEventId(lastPregEvent.getRelEventId()); //重新覆盖下relEventId

            //上一次妊娠检查事件
            doctorPigEventDao.delete(lastPregEvent.getId());
            //更新镜像
            DoctorPigSnapshot pregSnapshot = doctorPigSnapshotDao.findByToEventId(lastPregEvent.getId());
            List<DoctorPigSnapshot> snapshotList = doctorPigSnapshotDao.findByFromEventId(lastPregEvent.getId());
            if (snapshotList.isEmpty()) {
                doctorPigTrack.setCurrentEventId(pregSnapshot.getFromEventId());
                doctorPigTrackDao.update(doctorPigTrack);
            } else {
                doctorPigSnapshotDao.updateFromEventIdByFromEventId(snapshotList.stream().map(DoctorPigSnapshot::getId).collect(Collectors.toList()), pregSnapshot.getFromEventId());
            }
            //删掉镜像里的数据
            doctorPigSnapshotDao.deleteByEventId(lastPregEvent.getId());
        }

        return doctorPigEvent;
    }

    @Override
    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack){
        super.specialHandle(doctorPigEvent, doctorPigTrack);
        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Pregnancy.getKey())) {
            //对应的最近一次的 周期配种的初陪 的 isImpregnation 字段变成true
            DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
            expectTrue(notNull(firstMate), "first.mate.not.null", doctorPigEvent.getPigId());
            firstMate.setIsImpregnation(1);
            doctorPigEventDao.update(firstMate);
        }
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);

        Integer pregCheckResult = executeEvent.getPregCheckResult();

        //置当前配种数为0
        toTrack.setCurrentMatingCount(0);

        Map<String, Object> extra = toTrack.getExtraMap();
        //往extra增加一些特殊标志位用来表明配种类型
        if (Objects.equals(pregCheckResult, PregCheckResult.FANQING.getKey())) {
            extra.put("fanqingToMate", true);
            extra.put("pregCheckResult", KongHuaiPregCheckResult.FANQING.getKey());
        } else if (Objects.equals(pregCheckResult, PregCheckResult.YING.getKey())) {
            extra.put("yinToMate", true);
            extra.put("pregCheckResult", KongHuaiPregCheckResult.YING.getKey());
        } else if (Objects.equals(pregCheckResult, PregCheckResult.LIUCHAN.getKey())) {
            extra.put("liuchanToMateCheck", true);
            extra.put("pregCheckResult", KongHuaiPregCheckResult.LIUCHAN.getKey());
        }else if (Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())){
            extra.put("pregCheckResult", PigStatus.Pregnancy.getKey());
        }

        toTrack.setExtraMap(extra);

        if (Objects.equals(pregCheckResult, PregCheckResult.UNSURE.getKey())) {
            // 不修改状态
        } else if (Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())) {
            // 阳性
            toTrack.setStatus(PigStatus.Pregnancy.getKey());

            // 阳性在产房，设置为待分娩
            if (Objects.equals(toTrack.getCurrentBarnType(), PigType.DELIVER_SOW.getValue())) {
                toTrack.setStatus(PigStatus.Farrow.getKey());
            }

        } else {
            // 其余默认 没有怀孕
            toTrack.setStatus(PigStatus.KongHuai.getKey());
        }
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return toTrack;
    }

    //校验能否置成此妊娠检查状态
    private static void checkCanPregCheckResult(Integer pigStatus, Integer checkResult, String pigCode) {
        //已配种状态直接返回
        if (Objects.equals(pigStatus, PigStatus.Mate.getKey())) {
            return;
        }

        //阳性(待分娩)只能到空怀状态
        if (Objects.equals(pigStatus, PigStatus.Pregnancy.getKey()) || Objects.equals(pigStatus, PigStatus.Farrow.getKey())) {
            if (!PregCheckResult.KONGHUAI_RESULTS.contains(checkResult)) {
                throw new InvalidException("pregnancy.only.to.konghuai", PregCheckResult.from(checkResult).getDesc(), pigCode);
            }
            return;
        }

        //空怀或流程只能到阳性状态
        if (Objects.equals(pigStatus, PigStatus.KongHuai.getKey())) {
            if (!Objects.equals(checkResult, PregCheckResult.YANG.getKey())) {
                throw new InvalidException("konghuai.only.to.pregnancy", PregCheckResult.from(checkResult).getDesc(), pigCode);
            }
            return;
        }
        //如果不是 已配种, 妊娠检查结果状态, 不允许妊娠检查
        throw new InvalidException("sow.not.allow.preg.check", PigStatus.from(pigStatus).getName(), pigCode);
    }
}
