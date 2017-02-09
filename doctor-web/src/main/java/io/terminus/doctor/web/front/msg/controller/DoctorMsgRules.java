package io.terminus.doctor.web.front.msg.controller;

import com.google.api.client.util.Lists;
import com.google.common.base.Preconditions;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorMessageRule;
import io.terminus.doctor.event.model.DoctorMessageRuleRole;
import io.terminus.doctor.event.service.DoctorMessageRuleReadService;
import io.terminus.doctor.event.service.DoctorMessageRuleRoleReadService;
import io.terminus.doctor.event.service.DoctorMessageRuleRoleWriteService;
import io.terminus.doctor.event.service.DoctorMessageRuleWriteService;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.doctor.web.front.msg.dto.MsgRoleDto;
import io.terminus.doctor.web.front.msg.dto.MsgRuleDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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

    private final DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService;
    private final DoctorMessageRuleRoleWriteService doctorMessageRuleRoleWriteService;

    private final SubRoleReadService subRoleReadService;

    private final DoctorFarmReadService doctorFarmReadService;


    @Autowired
    public DoctorMsgRules(DoctorMessageRuleReadService doctorMessageRuleReadService,
                          DoctorMessageRuleWriteService doctorMessageRuleWriteService,
                          DoctorMessageRuleRoleReadService doctorMessageRuleRoleReadService,
                          DoctorMessageRuleRoleWriteService doctorMessageRuleRoleWriteService,
                          SubRoleReadService subRoleReadService,
                          DoctorFarmReadService doctorFarmReadService) {
        this.doctorMessageRuleReadService = doctorMessageRuleReadService;
        this.doctorMessageRuleWriteService = doctorMessageRuleWriteService;
        this.doctorMessageRuleRoleReadService = doctorMessageRuleRoleReadService;
        this.doctorMessageRuleRoleWriteService = doctorMessageRuleRoleWriteService;
        this.subRoleReadService = subRoleReadService;
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     * 根据猪场id获取规则列表
     * @param farmId    猪场id
     * @return
     */
    @RequestMapping(value = "/rule/farmId", method = RequestMethod.GET)
    public List<DoctorMessageRule> listRulesByFarmId(@RequestParam Long farmId,
                                                     @RequestParam(required = false, name = "name") String templateName) {
        return RespHelper.or500(doctorMessageRuleReadService.findMessageRulesByFarmIdAndTemplateName(farmId, templateName));
    }

    /**
     * 根据规格id获取规则详情
     * @param id    规则id
     * @return
     */
    @RequestMapping(value = "/rule/detail", method = RequestMethod.GET)
    public DoctorMessageRule findDetailById(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageRuleReadService.findMessageRuleById(id));
    }

    /**
     * 更新规则
     * @param doctorMessageRule
     * @return
     */
    @RequestMapping(value = "/rule", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateRule(@RequestBody DoctorMessageRule doctorMessageRule) {
        Preconditions.checkNotNull(doctorMessageRule, "template.rule.not.null");
        return RespHelper.or500(doctorMessageRuleWriteService.updateMessageRule(doctorMessageRule));
    }

    /**
     * 获取与角色相关的规则(包含绑定和未绑定)
     * @return
     */
    @RequestMapping(value = "/rule/hasFlag", method = RequestMethod.GET)
    public List<MsgRuleDto> findHasCheckedRule(@RequestParam Long farmId,
                                               @RequestParam Long roleId) {
        List<MsgRuleDto> dtos = Lists.newArrayList();
        List<DoctorMessageRule> doctorMessageRules = RespHelper.or500(
                doctorMessageRuleReadService.findMessageRulesByFarmId(farmId));
        List<Long> ruleIds = findRolesByRoleId(roleId).stream().map(MsgRoleDto::getRuleId).collect(Collectors.toList());
        doctorMessageRules.forEach(doctorMessageRule -> {
            MsgRuleDto msgRuleDto = MsgRuleDto.builder()
                    .doctorMessageRule(doctorMessageRule)
                    .roleId(roleId)
                    .build();
            if (ruleIds.contains(doctorMessageRule.getId())) {
                msgRuleDto.setFlag(Boolean.TRUE);
            }
            dtos.add(msgRuleDto);
        });
        return dtos;
    }

    /**
     * 获取角色想绑定的规则
     * @param ruleId    规则id
     */
    @RequestMapping(value = "/role/ruleId", method = RequestMethod.GET)
    public List<MsgRoleDto> findRolesByRuleId(@RequestParam Long ruleId) {
        List<MsgRoleDto> dtos = Lists.newArrayList();
        List<DoctorMessageRuleRole> ruleRoles = RespHelper.or500(doctorMessageRuleRoleReadService.findByRuleId(ruleId));
        for (int i = 0; ruleRoles != null && i < ruleRoles.size(); i++) {
            dtos.add(createMsgRoleDto(ruleRoles.get(i)));
        }
        return dtos;
    }

    /**
     * 获取角色想绑定的规则
     * @param roleId    角色id
     * @return
     */
    @RequestMapping(value = "/role/roleId", method = RequestMethod.GET)
    public List<MsgRoleDto> findRolesByRoleId(@RequestParam Long roleId) {
        List<MsgRoleDto> dtos = Lists.newArrayList();
        List<DoctorMessageRuleRole> ruleRoles = RespHelper.or500(doctorMessageRuleRoleReadService.findByRoleId(roleId));
        for (int i = 0; ruleRoles != null && i < ruleRoles.size(); i++) {
            dtos.add(createMsgRoleDto(ruleRoles.get(i)));
        }
        return dtos;
    }

    private MsgRoleDto createMsgRoleDto(DoctorMessageRuleRole ruleRole) {
        MsgRoleDto msgRoleDto = BeanMapper.map(ruleRole, MsgRoleDto.class);
        // 获取角色信息
        SubRole subRole = RespHelper.or500(subRoleReadService.findById(ruleRole.getRoleId()));
        msgRoleDto.setRoleName(subRole == null ? null : subRole.getName());
        return msgRoleDto;
    }

    /**
     * 关联rule模板与角色
     * @return
     */
    @RequestMapping(value = "/role/relative/ruleId", method = RequestMethod.POST)
    public Boolean relateRuleRolesByRuleId(@RequestParam("ruleId") Long ruleId,
                               @RequestParam("roleIds") List<Long> roleIds) {
        Preconditions.checkNotNull(ruleId, "template.ruleId.not.null");
        return RespHelper.or500(doctorMessageRuleRoleWriteService.relateRuleRolesByRuleId(ruleId, roleIds));
    }

    /**
     * 关联rule模板与角色
     * @return
     */
    @RequestMapping(value = "/role/relative/roleId", method = RequestMethod.POST)
    public Boolean relateRuleRolesByRoleId(@RequestParam("roleId") Long roleId,
                                           @RequestParam("ruleIds") List<Long> ruleIds) {
        Preconditions.checkNotNull(roleId, "template.roleId.not.null");
        return RespHelper.or500(doctorMessageRuleRoleWriteService.relateRuleRolesByRoleId(roleId, ruleIds));
    }

    @RequestMapping(value = "/relative/farmId", method = RequestMethod.GET)
    public Boolean relateRuleFarmByFarmId(@RequestParam(value = "farmIds", required = false) String farmIds){
        Boolean result = true;
        List<Long> farmIdList;
        if (StringUtils.isNotBlank(farmIds)) {
            farmIdList = Splitters.UNDERSCORE.splitToList(farmIds).stream().map(farmId -> Long.parseLong(farmId)).collect(Collectors.toList());

        }else {
            farmIdList = RespHelper.or500(doctorFarmReadService.findAllFarms()).stream().map(doctorFarm -> doctorFarm.getId()).collect(Collectors.toList());
        }
        for (Long farmId : farmIdList){
            result &= RespHelper.or500(doctorMessageRuleWriteService.initTemplate(farmId));
        }
        return result;
    }

    /**
     * 禁用启用规则
     * @param farmId
     * @param templateId
     * @param status
     * @return
     */
    @RequestMapping(value = "/rule/chgStatus", method = RequestMethod.GET)
    public Boolean changeStatus(@RequestParam(value = "farmId") Long farmId,
                                @RequestParam(value = "templateId") Long templateId,
                                @RequestParam(value = "status") Integer status) {
        DoctorMessageRule rule = DoctorMessageRule.builder()
                .farmId(farmId)
                .templateId(templateId)
                .build();
        try {
            DoctorMessageRule doctorMessageRule = RespHelper.or500(doctorMessageRuleReadService.findMessageRulesByCriteria(BeanMapper.convertObjectToMap(rule))).get(0);
            doctorMessageRule.setStatus(status);
            doctorMessageRuleWriteService.updateMessageRule(doctorMessageRule);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("change status failed");
        }
        return Boolean.FALSE;
    }

}
