package io.terminus.doctor.open.rest.farm;

import com.google.common.collect.Lists;
import io.terminus.doctor.open.dto.DoctorBasicDto;
import io.terminus.doctor.open.dto.DoctorFarmBasicDto;
import io.terminus.doctor.open.dto.DoctorStatisticDto;
import io.terminus.doctor.open.rest.farm.service.DoctorStatisticReadService;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
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
     * 查询单个猪场信息
     * 猪场id
     * @return 猪场信息
     * @see DoctorStatisticReadService#getFarmStatistic(java.lang.Long) 正式接口
     */
    @OpenMethod(key = "get.farm.info", paramNames = "farmId")
    public DoctorFarmBasicDto getFarmInfo(@NotNull(message = "farmId.not.null") Long farmId) {
        return new DoctorFarmBasicDto(
                OPRespHelper.orOPEx(doctorFarmReadService.findFarmById(1L)),
                mockFarmStats()
        );
    }

    /**
     * 根据用户id查询所拥有权限的猪场信息
     * @return 猪场信息list
     * @see DoctorStatisticReadService#getOrgStatistic(java.lang.Long) 正式接口
     */
    @OpenMethod(key = "get.company.info")
    public DoctorBasicDto getCompanyInfo() {
        return new DoctorBasicDto(
                        OPRespHelper.orOPEx(doctorFarmReadService.findOrgByUserId(UserUtil.getUserId())),
                        mockOrgStats(),
                Lists.newArrayList(
                        new DoctorFarmBasicDto(
                                OPRespHelper.orOPEx(doctorFarmReadService.findFarmById(1L)),
                                mockFarmStats()
                        ),
                        new DoctorFarmBasicDto(
                                OPRespHelper.orOPEx(doctorFarmReadService.findFarmById(2L)),
                                mockFarmStats()
                        )
                )
        );
    }

    private List<DoctorStatisticDto> mockOrgStats() {
        return Lists.newArrayList(
                new DoctorStatisticDto(DoctorStatisticDto.PigType.SOW.getDesc(), 100),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FARROW_PIGLET.getDesc(), 200),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.NURSERY_PIGLET.getDesc(), 300),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FATTEN_PIG.getDesc(), 400),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.BREEDING_PIG.getDesc(), 50)
        );
    }

    private List<DoctorStatisticDto> mockFarmStats() {
        return Lists.newArrayList(
                new DoctorStatisticDto(DoctorStatisticDto.PigType.SOW.getCutDesc(), 100),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FARROW_PIGLET.getCutDesc(), 200),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.NURSERY_PIGLET.getCutDesc(), 300),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FATTEN_PIG.getCutDesc(), 400),
                new DoctorStatisticDto(DoctorStatisticDto.PigType.BREEDING_PIG.getCutDesc(), 50)
        );
    }
}
