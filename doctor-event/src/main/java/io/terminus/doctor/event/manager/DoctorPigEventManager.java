package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarFarmEntryDto;
import io.terminus.doctor.event.dto.event.sow.DoctorSowFarmEntryDto;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪事件信息录入管理过程
 */
@Component
@Slf4j
public class DoctorPigEventManager {

    /**
     * 进厂事件(不进行数据校验信息)
     * @return
     */
    @Transactional
    public Boolean entrySowFarmEvent(DoctorSowFarmEntryDto doctorSowFarmEntryDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto){

        //母猪表
//        DoctorPig doctorPig =

        //pig track

        //snapshot

        //event

        return null;
    }

    /**
     * 公猪进厂事件
     * @return
     */
    public Boolean entryBoarFarmEvent(DoctorBoarFarmEntryDto doctorBoarFarmEntryDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto){
        return null;
    }

    /**
     * 构建DoctorPig
     * @param dto
     * @param basic
     * @return
     */
    public DoctorPig buildDoctorPig(DoctorSowFarmEntryDto dto, DoctorBasicInputInfoDto basic){

//        if(isNull(doctorBasicInputInfoDto.getFarmId())||isNull(doctorSowFarmEntryDto.getPigCode())){
//            return null;
//        }
//
//        return DoctorPig.builder()
//                .farmId(basic.getFarmId()).farmName(basic.getFarmName()).orgId(basic.getOrgId()).orgName(basic.getOrgName())
//                .build();
        return null;

    }

}
