package io.terminus.doctor.web.front.msg.controller;

import com.google.common.base.Preconditions;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Desc: 与消息模板与猪场绑定相关
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/7
 */
@RestController
@Slf4j
@RequestMapping("/api/doctor/msg")
public class DoctorMsgRules {

    private final DoctorMessageRuleReadService doctorMessageRuleReadService;
    private final DoctorMessageRuleWriteService doctorMessageRuleWriteService;

    public DoctorMsgRules(DoctorMessageRuleReadService doctorMessageRuleReadService,
                          DoctorMessageRuleWriteService doctorMessageRuleWriteService) {
        this.doctorMessageRuleReadService = doctorMessageRuleReadService;
        this.doctorMessageRuleWriteService = doctorMessageRuleWriteService;
    }

    /**
     * 根据猪场id获取规则列表
     * @param farmId    猪场id
     * @return
     */
    @RequestMapping(value = "/rule/farm", method = RequestMethod.GET)
    public List<DoctorMessageRule> listRulesByFarmId(@RequestParam Long farmId) {
        return RespHelper.or500(doctorMessageRuleReadService.findMessageRulesByFarmId(farmId));
    }

    /**
     * 根据规格id获取规则详情
     * @param ruleId    规则id
     * @return
     */
    @RequestMapping(value = "/rule/detail", method = RequestMethod.GET)
    public DoctorMessageRule findDetailById(@RequestParam Long ruleId) {
        return RespHelper.or500(doctorMessageRuleReadService.findMessageRuleById(ruleId));
    }

    /**
     * 更新规则
     * @param doctorMessageRule
     * @return
     */
    @RequestMapping(value = "/rule", method = RequestMethod.POST)
    public Boolean updateRule(@RequestBody DoctorMessageRule doctorMessageRule) {
        Preconditions.checkNotNull(doctorMessageRule, "template.rule.not.null");
        return RespHelper.or500(doctorMessageRuleWriteService.updateMessageRule(doctorMessageRule));
    }
}
