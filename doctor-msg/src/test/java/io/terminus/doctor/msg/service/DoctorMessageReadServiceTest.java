package io.terminus.doctor.msg.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.BaseServiceTest;
import io.terminus.doctor.msg.dto.DoctorMessageSearchDto;
import io.terminus.doctor.msg.model.DoctorMessage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xiao on 16/9/4.
 */
public class DoctorMessageReadServiceTest extends BaseServiceTest {

    @Autowired
    private DoctorMessageReadService doctorMessageReadService;

    @Autowired
    private DoctorMessageRuleReadService doctorMessageRuleReadService;

    @Test
    public void test_pagingWarnMessage(){
        DoctorMessageSearchDto doctorMessage = (DoctorMessageSearchDto) DoctorMessage.builder()
              //  .isExpired(DoctorMessage.IsExpired.NOTEXPIRED.getValue())
                .farmId(1l)
                .templateId(9l)
                .build();
        Response<Paging<DoctorMessage>> pagingResponse = doctorMessageReadService.pagingWarnMessages(doctorMessage, 0, 100);
        System.out.println(pagingResponse.getResult().getTotal());
    }

    @Test
    public void test_rulePigCount(){

////        List<DoctorMessageRule> doctorMessageRules = RespHelper.or500(doctorMessageRuleReadService.findMessageRulesByCriteria(ImmutableMap.of("farmId", 1, "types", ImmutableList.of(DoctorMessageRuleTemplate.Type.WARNING, DoctorMessageRuleTemplate.Type.ERROR))));
////        doctorMessageRules.forEach(doctorMessageRule -> {
//            int pigCount = RespHelper.or500(doctorMessageReadService.findBusinessListByCriteria(ImmutableMap.of("templateId", 11, "farmId", 1, "isExpired", DoctorMessage.IsExpired.NOTEXPIRED.getValue()))).size();
////            System.out.println(doctorMessageRule.getTemplateName() + "-" + pigCount);
////        });
    }

    @Test
    public void test_queryBusinessId(){
//        DoctorMessageSearchDto doctorMessage = new DoctorMessageSearchDto();
//                doctorMessage.setFarmId(1l);
//                doctorMessage.setStatus(DoctorMessage.Status.NORMAL.getValue());
//                doctorMessage.setIsExpired(DoctorMessage.IsExpired.NOTEXPIRED.getValue());
//        Response<List<Long>> listResponse = doctorMessageReadService.findBusinessListByCriteria(doctorMessage);
//        System.out.println(listResponse.getResult().size());
    }
}
