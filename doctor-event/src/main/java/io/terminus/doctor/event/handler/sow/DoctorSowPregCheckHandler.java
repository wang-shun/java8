package io.terminus.doctor.event.handler.sow;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

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
    public void handleCheck(BasePigEventInputDto eventDto, DoctorBasicInputInfoDto basic) {
        super.handleCheck(eventDto, basic);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(eventDto.getPigId());
        DoctorPregChkResultDto pregChkResultDto = (DoctorPregChkResultDto) eventDto;
        checkCanPregCheckResult(doctorPigTrack.getStatus(), pregChkResultDto.getCheckResult(), pregChkResultDto.getPigCode());

    }

    @Override
    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorPregChkResultDto pregChkResultDto = (DoctorPregChkResultDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pregChkResultDto.getPigId());
        //妊娠检查结果，从extra中拆出来
        Integer pregCheckResult = pregChkResultDto.getCheckResult();
        doctorPigEvent.setPregCheckResult(pregCheckResult);

        //妊娠检查事件时间
        DateTime checkDate = new DateTime(pregChkResultDto.eventAt());
        doctorPigEvent.setCheckDate(checkDate.toDate());

        //查找最近一次配种事件
        DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        if (notNull(lastMate) && !Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())) {
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
                doctorPigEvent.setPlnpd(doctorPigEvent.getPlnpd() + npd);
                doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
            }

        }

        //根据猪类判断, 如果是逆向: 空怀 => 阳性, 需要删掉以前的空怀事件
        if ((Objects.equals(doctorPigTrack.getStatus(), PigStatus.KongHuai.getKey()))) {
            DoctorPigEvent lastPregEvent = doctorPigEventDao.queryLastPregCheck(doctorPigTrack.getPigId());
            if (lastPregEvent == null || !PregCheckResult.KONGHUAI_RESULTS.contains(lastPregEvent.getPregCheckResult())) {
                throw new ServiceException("不允许妊娠检查,猪号:" + pregChkResultDto.getPigCode());
            }

            log.info("remove old preg check event info:{}", lastPregEvent);
            doctorPigEvent.setId(lastPregEvent.getId());    //把id放进去, 用于更新数据
            doctorPigEvent.setRelEventId(lastPregEvent.getRelEventId()); //重新覆盖下relEventId

            //上一次妊娠检查事件
            doctorPigEventDao.delete(lastPregEvent.getId());
            //删掉镜像里的数据
            doctorPigSnapshotDao.deleteByEventId(lastPregEvent.getId());
        }

        return doctorPigEvent;
    }

    @Override
    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic){
        super.specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);
        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Pregnancy.getKey())) {
            //对应的最近一次的 周期配种的初陪 的 isImpregnation 字段变成true
            DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
            if (notNull(firstMate)) {
                firstMate.setIsImpregnation(1);
                doctorPigEventDao.update(firstMate);
            }
        }
    }

    @Override
    public DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPregChkResultDto pregChkResultDto = (DoctorPregChkResultDto) inputDto;
        Integer pregCheckResult = pregChkResultDto.getCheckResult();
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pregChkResultDto.getPigId());

        //如果妊娠检查非阳性, 置当前配种数为0
        doctorPigTrack.setCurrentMatingCount(0);

        Map<String, Object> extra = doctorPigTrack.getExtraMap();
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

        doctorPigTrack.setExtraMap(extra);

        if (Objects.equals(pregCheckResult, PregCheckResult.UNSURE.getKey())) {
            // 不修改状态
        } else if (Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())) {
            // 阳性
            doctorPigTrack.setStatus(PigStatus.Pregnancy.getKey());
        } else {
            // 其余默认 没有怀孕
            doctorPigTrack.setStatus(PigStatus.KongHuai.getKey());
        }
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    //校验能否置成此妊娠检查状态
    private static void checkCanPregCheckResult(Integer pigStatus, Integer checkResult, String pigCode) {
        //已配种状态直接返回
        if (Objects.equals(pigStatus, PigStatus.Mate.getKey())) {
            return;
        }

        //阳性只能到空怀状态
        if (Objects.equals(pigStatus, PigStatus.Pregnancy.getKey())) {
            if (!PregCheckResult.KONGHUAI_RESULTS.contains(checkResult)) {
                throw new ServiceException("妊娠检查结果错误,猪号:" + pigCode);
            }
            return;
        }

        //空怀或流程只能到阳性状态
        if (Objects.equals(pigStatus, PigStatus.KongHuai.getKey())) {
            if (!Objects.equals(checkResult, PregCheckResult.YANG.getKey())) {
                throw new ServiceException("妊娠检查结果错误,猪号:" + pigCode);
            }
            return;
        }

        //返情只能到阳性状态(以后全是空怀了)
        if (Objects.equals(pigStatus, PigStatus.FanQing.getKey())) {
            if (!Objects.equals(checkResult, PregCheckResult.YANG.getKey())) {
                throw new ServiceException("妊娠检查结果错误,猪号:" + pigCode);
            }
            return;
        }

        //如果不是 已配种, 妊娠检查结果状态, 不允许妊娠检查
        throw new ServiceException("不允许妊娠检查,猪号:" + pigCode);
    }
}
