package io.terminus.doctor.msg.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.BaseServiceTest;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

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
        Map<String, Object> map = Maps.newHashMap();
        map.put("templateId", 9l);
        map.put("farmId", 1l);
        map.put("isExpired", 0);
        Response<Paging<DoctorMessage>> pagingResponse = doctorMessageReadService.pagingWarnMessages(map, 0, 100);
        System.out.println(pagingResponse.getResult().getTotal());
    }

    @Test
    public void test_rulePigCount(){

        List<DoctorMessageRule> doctorMessageRules = RespHelper.or500(doctorMessageRuleReadService.findMessageRulesByCriteria(ImmutableMap.of("farmId", 1, "types", ImmutableList.of(DoctorMessageRuleTemplate.Type.WARNING, DoctorMessageRuleTemplate.Type.ERROR))));
        doctorMessageRules.forEach(doctorMessageRule -> {
            Long pigCount = RespHelper.or500(doctorMessageReadService.findMessageCountByCriteria(ImmutableMap.of("templateId", doctorMessageRule.getTemplateId(), "farmId", doctorMessageRule.getFarmId(), "isExpired", DoctorMessage.IsExpired.NOTEXPIRED.getValue())));
            System.out.println(doctorMessageRule.getTemplateName() + "-" + pigCount);
        });
    }
}
