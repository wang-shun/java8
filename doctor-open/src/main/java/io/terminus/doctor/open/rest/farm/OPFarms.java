package io.terminus.doctor.open.rest.farm;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorStatisticDto;
import io.terminus.doctor.open.dto.DoctorBasicDto;
import io.terminus.doctor.open.dto.DoctorFarmBasicDto;
import io.terminus.doctor.open.dto.DoctorOrgBasicDto;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@OpenBean
@SuppressWarnings("unused")
public class OPFarms {

    private final DoctorFarmReadService doctorFarmReadService;

    @Autowired
    private OPFarms(DoctorFarmReadService doctorFarmReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     * 查询公司概况
     * @return 公司信息
     */
    @OpenMethod(key = "get.company.info")
    public DoctorOrg getOrgInfo() {
        return OPRespHelper.orOPEx(doctorFarmReadService.findOrgByUserId(UserUtil.getUserId()));
    }

    /**
     * 根据用户id查询所拥有权限的猪场信息
     * @return 猪场信息list
     */
    @OpenMethod(key = "get.farm.info")
    public DoctorBasicDto getFarmInfo() {
        return new DoctorBasicDto(
                new DoctorOrgBasicDto(
                        OPRespHelper.orOPEx(doctorFarmReadService.findOrgByUserId(UserUtil.getUserId())),
                        mockStats()
                ),
                Lists.newArrayList(
                        new DoctorFarmBasicDto(
                                OPRespHelper.orOPEx(doctorFarmReadService.findFarmById(1L)),
                                mockStats()
                        ),
                        new DoctorFarmBasicDto(
                                OPRespHelper.orOPEx(doctorFarmReadService.findFarmById(2L)),
                                mockStats()
                        )
                )
        );
    }

    private List<DoctorStatisticDto> mockStats() {
        return Lists.newArrayList(
                new DoctorStatisticDto(DoctorStatisticDto.PigType.SOW.getDesc(), 100),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FARROW_PIGLET.getDesc(), 200),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.NURSERY_PIGLET.getDesc(), 300),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FATTEN_PIG.getDesc(), 400),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.BREEDING_PIG.getDesc(), 50)
        );
    }
}
