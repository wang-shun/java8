package io.terminus.doctor.web.front.msg.controller;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.DoctorMessageUserDto;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.msg.model.DoctorMessageUser;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateWriteService;
import io.terminus.doctor.msg.service.DoctorMessageUserReadService;
import io.terminus.doctor.msg.service.DoctorMessageUserWriteService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
import io.terminus.doctor.web.front.msg.dto.DoctorMessageDto;
import io.terminus.doctor.web.front.msg.dto.DoctorMessageWithUserDto;
import io.terminus.doctor.web.front.msg.dto.OneLevelMessageDto;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc: 与消息和消息模板相关
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/6
 */
@RestController
@Slf4j
@RequestMapping("/api/doctor/msg")
public class DoctorMessages {

    private final DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;
    private final DoctorMessageRuleTemplateWriteService doctorMessageRuleTemplateWriteService;
    private final DoctorMessageReadService doctorMessageReadService;
    private final DoctorMessageWriteService doctorMessageWriteService;
    private final DoctorMessageRuleReadService doctorMessageRuleReadService;
    private final DoctorMessageUserReadService doctorMessageUserReadService;
    private final DoctorMessageUserWriteService doctorMessageUserWriteService;

    @Autowired
    public DoctorMessages(DoctorMessageReadService doctorMessageReadService,
                          DoctorMessageWriteService doctorMessageWriteService,
                          DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                          DoctorMessageRuleTemplateWriteService doctorMessageRuleTemplateWriteService,
                          DoctorMessageRuleReadService doctorMessageRuleReadService,
                          DoctorMessageUserReadService doctorMessageUserReadService,
                          DoctorMessageUserWriteService doctorMessageUserWriteService) {
        this.doctorMessageReadService = doctorMessageReadService;
        this.doctorMessageWriteService = doctorMessageWriteService;
        this.doctorMessageRuleTemplateReadService = doctorMessageRuleTemplateReadService;
        this.doctorMessageRuleTemplateWriteService = doctorMessageRuleTemplateWriteService;
        this.doctorMessageRuleReadService = doctorMessageRuleReadService;
        this.doctorMessageUserReadService = doctorMessageUserReadService;
        this.doctorMessageUserWriteService = doctorMessageUserWriteService;
    }


    /************************** 消息相关 **************************/

    /**
     * 查询预警消息分页
     *
     * @param pageNo   页码
     * @param pageSize 页大小
     * @param criteria 参数
     * @return
     */
    @RequestMapping(value = "/warn/messages", method = RequestMethod.GET)
    public DoctorMessageDto pagingWarnDoctorMessages(@RequestParam("pageNo") Integer pageNo,
                                                                     @RequestParam("pageSize") Integer pageSize,
                                                                     @RequestParam Map<String, String> criteria) {
        if (!isUserLogin()) {
            return new DoctorMessageDto(new Paging<>(0L, Collections.emptyList()),null);
        }
        DoctorMessageUserDto doctorMessageUserDto = new DoctorMessageUserDto();
        doctorMessageUserDto.setUserId(UserUtil.getUserId());
        doctorMessageUserDto.setTemplateId(Long.parseLong(criteria.get("templateId")));
        doctorMessageUserDto.setFarmId(Long.parseLong(criteria.get("farmId")));
        Paging<DoctorMessageUser> doctorMessageUserPaging = RespHelper.or500(doctorMessageUserReadService.paging(doctorMessageUserDto, pageNo, pageSize));
        DoctorMessageDto msgDto = DoctorMessageDto.builder().build();
        List<DoctorMessageWithUserDto> list = Lists.newArrayList();
        if (doctorMessageUserPaging.getData() != null && doctorMessageUserPaging.getData().size() > 0) {
            doctorMessageUserPaging.getData().forEach(doctorMessageUser -> {
                DoctorMessage doctorMessage = RespHelper.or500(doctorMessageReadService.findMessageById(doctorMessageUser.getMessageId()));
                String urlPart;
                if (Objects.equals(doctorMessage.getCategory(), Category.FATTEN_PIG_REMOVE.getKey())) {
                    urlPart = "?groupId=";
                    msgDto.setListUrl("/group/list?farmId=" + doctorMessage.getFarmId() + "&searchFrom=MESSAGE");
                } else if (Objects.equals(doctorMessage.getCategory(), Category.STORAGE_SHORTAGE.getKey())) {
                    urlPart = "?materialId=";
                } else {
                    urlPart = "?pigId=";
                    if (Objects.equals(doctorMessage.getCategory(), Category.BOAR_ELIMINATE.getKey())) {
                        msgDto.setListUrl("/boar/list?farmId=" + doctorMessage.getFarmId() + "&searchFrom=MESSAGE");
                    } else {
                        msgDto.setListUrl("/sow/list?farmId=" + doctorMessage.getFarmId() + "&searchFrom=MESSAGE");
                    }
                }
                doctorMessage.setUrl(doctorMessage.getUrl().concat(urlPart + doctorMessage.getBusinessId() + "&farmId=" + doctorMessage.getFarmId()));
                list.add(new DoctorMessageWithUserDto(doctorMessage, doctorMessageUser));
            });
        }
        msgDto.setPaging(new Paging<>(doctorMessageUserPaging.getTotal(), list));
        return msgDto;
    }

    /**
     * 查询系统消息分页
     *
     * @param pageNo   页码
     * @param pageSize 页大小
     * @param criteria 参数
     * @return
     */
    @RequestMapping(value = "/sys/messages", method = RequestMethod.GET)
    public Paging<DoctorMessage> pagingSysDoctorMessages(@RequestParam("pageNo") Integer pageNo,
                                                         @RequestParam("pageSize") Integer pageSize,
                                                         @RequestParam Map<String, Object> criteria) {
        if (!isUserLogin()) {
            return new Paging<>(0L, Collections.emptyList());
        }
        criteria.put("userId", UserUtil.getUserId());
        return RespHelper.or500(doctorMessageReadService.pagingSysMessages(criteria, pageNo, pageSize));
    }

    /**
     * 查询未读消息数量
     *
     * @return
     */
    @RequestMapping(value = "/noReadCount", method = RequestMethod.GET)
    public Long findNoReadCount() {
        if (!isUserLogin()) {
            return 0L;
        }
        return RespHelper.or500(doctorMessageUserReadService.findNoReadCount(UserUtil.getUserId()));
    }

    /**
     * 查询消息详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/message/detail", method = RequestMethod.GET)
    public Boolean findMessageDetail(@RequestParam("id") Long id) {
        DoctorMessageUserDto doctorMessageUserDto = new DoctorMessageUserDto();
        doctorMessageUserDto.setUserId(UserUtil.getUserId());
        doctorMessageUserDto.setMessageId(id);
        DoctorMessageUser doctorMessageUser = RespHelper.or500(doctorMessageUserReadService.findDoctorMessageUsersByCriteria(doctorMessageUserDto)).get(0);
        if (doctorMessageUser != null) {
            // 如果消息是未读, 将消息设置为已读
            doctorMessageUser.setStatusSys(DoctorMessageUser.Status.READED.getValue());
            doctorMessageUserWriteService.updateDoctorMessageUser(doctorMessageUser);
            return true;
        }
       return false;
    }

    /**
     * 更新消息
     *
     * @param doctorMessage
     * @return
     */
    @RequestMapping(value = "/message", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateMessage(@RequestBody DoctorMessage doctorMessage) {
        Preconditions.checkNotNull(doctorMessage, "message.not.null");
        if (doctorMessage.getId() == null) {
            doctorMessage.setCreatedBy(UserUtil.getUserId());
            RespHelper.or500(doctorMessageWriteService.createMessage(doctorMessage));
        } else {
            RespHelper.or500(doctorMessageWriteService.updateMessage(doctorMessage));
        }
        return Boolean.TRUE;
    }

    /**
     * 删除消息
     *
     * @param id 消息id
     * @return
     */
    @RequestMapping(value = "/message", method = RequestMethod.DELETE)
    public Boolean deleteMessage(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageWriteService.deleteMessageById(id));
    }


    /************************** 消息模板相关 **************************/

    /**
     * 查询系统消息模板列表
     *
     * @param criteria
     * @return
     */
    @RequestMapping(value = "/sys/templates", method = RequestMethod.GET)
    public List<DoctorMessageRuleTemplate> listSysTemplate(Map<String, Object> criteria) {
        criteria.put("type", DoctorMessageRuleTemplate.Type.SYSTEM.getValue());
        return RespHelper.or500(doctorMessageRuleTemplateReadService.findTemplatesByCriteria(criteria));
    }

    /**
     * 查询预警消息模板列表
     *
     * @param criteria
     * @return
     */
    @RequestMapping(value = "/warn/templates", method = RequestMethod.GET)
    public List<DoctorMessageRuleTemplate> listWarnTemplate(Map<String, Object> criteria) {
        criteria.put("types", ImmutableList.of(
                DoctorMessageRuleTemplate.Type.WARNING.getValue(), DoctorMessageRuleTemplate.Type.ERROR.getValue()));
        return RespHelper.or500(doctorMessageRuleTemplateReadService.findTemplatesByCriteria(criteria));
    }

    /**
     * 根据id获取模板信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/template/detail", method = RequestMethod.GET)
    public DoctorMessageRuleTemplate getTemplateById(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(id));
    }

    /**
     * 创建或者更新模板
     *
     * @param doctorMessageRuleTemplate
     * @return
     */
    @RequestMapping(value = "/template", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateTemplate(@RequestBody DoctorMessageRuleTemplate doctorMessageRuleTemplate) {
        Preconditions.checkNotNull(doctorMessageRuleTemplate, "template.not.null");
        if (doctorMessageRuleTemplate.getId() == null) {
            doctorMessageRuleTemplate.setUpdatedBy(UserUtil.getUserId());
            RespHelper.or500(doctorMessageRuleTemplateWriteService.createMessageRuleTemplate(doctorMessageRuleTemplate));
        } else {
            doctorMessageRuleTemplate.setUpdatedBy(UserUtil.getUserId());
            RespHelper.or500(doctorMessageRuleTemplateWriteService.updateMessageRuleTemplate(doctorMessageRuleTemplate));
        }
        return Boolean.TRUE;
    }

    /**
     * 删除一个模板
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/template", method = RequestMethod.DELETE)
    public Boolean deleteTemplate(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageRuleTemplateWriteService.deleteMessageRuleTemplateById(id));
    }

    /**
     * 获取猪场中消息规则与相应猪只数,列表
     *
     * @return
     */
    @RequestMapping(value = "/warn/rule/list", method = RequestMethod.GET)
    public List<OneLevelMessageDto> getRulePigCountByFarmId(@RequestParam("farmId") Long farmId) {
        List<OneLevelMessageDto> list = Lists.newArrayList();
        Map<String, Object> criteriaMap = Maps.newHashMap();
        criteriaMap.put("farmId", farmId);
        criteriaMap.put("types", ImmutableList.of(DoctorMessageRuleTemplate.Type.WARNING, DoctorMessageRuleTemplate.Type.ERROR));
        List<DoctorMessageRule> doctorMessageRules = RespHelper.or500(doctorMessageRuleReadService.findMessageRulesByCriteria(criteriaMap));
        doctorMessageRules.forEach(doctorMessageRule -> {
            Integer pigCount = 0;
            DoctorMessageUserDto doctorMessageUserDto = new DoctorMessageUserDto();
            doctorMessageUserDto.setFarmId(farmId);
            doctorMessageUserDto.setTemplateId(doctorMessageRule.getTemplateId());
            doctorMessageUserDto.setUserId(UserUtil.getUserId());
            //统计育肥猪出栏消息头数
            if (Objects.equals(doctorMessageRule.getCategory(), Category.FATTEN_PIG_REMOVE.getKey())) {
                try {
                    List<DoctorMessageUser> messageUsers = RespHelper.or500(doctorMessageUserReadService.findDoctorMessageUsersByCriteria(doctorMessageUserDto));
                    List<Long> ids = messageUsers.stream().map(DoctorMessageUser::getMessageId).collect(Collectors.toList());
                    List<DoctorMessage> messages = RespHelper.or500(doctorMessageReadService.findMessagesByIds(ids));
                    for (DoctorMessage doctorMessage : messages) {
                        Map<String, Object> dataMap = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(doctorMessage.getData(), JacksonType.MAP_OF_OBJECT);
                        pigCount += (Integer) dataMap.get("quantity");
                    }

                } catch (Exception e) {
                    log.error("get.rule.pig.count.failed");
                }
            } else if (!Objects.equals(doctorMessageRule.getCategory(), Category.STORAGE_SHORTAGE.getKey())) {
                pigCount = RespHelper.or500(doctorMessageUserReadService.findBusinessListByCriteria(doctorMessageUserDto)).size();
            }
            DoctorMessageRule messageRule = BeanMapper.map(doctorMessageRule, DoctorMessageRule.class);
            messageRule.setRuleValue("");
            list.add(OneLevelMessageDto.builder()
                    .doctorMessageRule(messageRule)
                    .pigCount(pigCount)
                    .build());

        });
        return list;
    }

    /**
     * 判断当前用户是否登录
     *
     * @return 如果登录: true
     * 否则:    false
     */
    private boolean isUserLogin() {
        return UserUtil.getCurrentUser() != null;
    }

}
