package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupEventSearchDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiao on 16/9/13.
 */
@Component
public class DoctorGroupEventReadServiceTest extends BaseServiceTest {
    @Autowired
    private DoctorGroupReadService doctorGroupReadService;

    @Test
    public void test_queryGroupEventsByCriteria(){
        DoctorGroupEventSearchDto doctorGroupEventSearchDto = new DoctorGroupEventSearchDto();
        doctorGroupEventSearchDto.setType(GroupEventType.CHANGE.getValue());
        Paging<DoctorGroupEvent> paging = RespHelper.orServEx(doctorGroupReadService.queryGroupEventsByCriteria(doctorGroupEventSearchDto, 0, 1));
        System.out.println(paging.getTotal());

    }

    @Test
    public void test(){
        List<PigEvent> list = PigEvent.from(Splitters.UNDERSCORE.splitToList("0_1").stream().filter(type -> StringUtils.isNotBlank(type)).map(type -> Integer.parseInt(type)).collect(Collectors.toList()));
        System.out.println(list);
    }
}
