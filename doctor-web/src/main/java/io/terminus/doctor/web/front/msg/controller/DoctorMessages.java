package io.terminus.doctor.web.front.msg.controller;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.dto.Rule;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.web.core.component.UserResService;
import io.terminus.doctor.web.front.msg.dto.DoctorMessageDto;
import io.terminus.doctor.web.front.msg.dto.OneLevelMessageDto;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import io.terminus.doctor.msg.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.msg.service.DoctorMessageReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.msg.service.DoctorMessageRuleTemplateWriteService;
import io.terminus.doctor.msg.service.DoctorMessageWriteService;
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
    private final UserResService userResService;

    @Autowired
    public DoctorMessages(DoctorMessageReadService doctorMessageReadService,
                          DoctorMessageWriteService doctorMessageWriteService,
                          DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService,
                          DoctorMessageRuleTemplateWriteService doctorMessageRuleTemplateWriteService,
                          DoctorMessageRuleReadService doctorMessageRuleReadService,
                          UserResService userResService) {
        this.doctorMessageReadService = doctorMessageReadService;
        this.doctorMessageWriteService = doctorMessageWriteService;
        this.doctorMessageRuleTemplateReadService = doctorMessageRuleTemplateReadService;
        this.doctorMessageRuleTemplateWriteService = doctorMessageRuleTemplateWriteService;
        this.doctorMessageRuleReadService = doctorMessageRuleReadService;
        this.userResService = userResService;
    }


    /************************** 消息相关 **************************/

    /**
     * 查询预警消息分页
     * @param pageNo    页码
     * @param pageSize  页大小
     * @param criteria  参数
     * @return
     */
    @RequestMapping(value = "/warn/messages", method = RequestMethod.GET)
    public DoctorMessageDto pagingWarnDoctorMessages(@RequestParam("pageNo") Integer pageNo,
                                                      @RequestParam("pageSize") Integer pageSize,
                                                      @RequestParam Map<String, Object> criteria) {
        if (!isUserLogin()) {
            return new DoctorMessageDto(new Paging<>(0L, Collections.emptyList()), null);
        }
        criteria.put("userId", UserUtil.getUserId());
        criteria.put("isExpired", DoctorMessage.IsExpired.NOTEXPIRED);
        Paging<DoctorMessage> paging = RespHelper.or500(doctorMessageReadService.pagingWarnMessages(criteria, pageNo, pageSize));
        List<DoctorMessage> messages = paging.getData();

        DoctorMessageDto msgDto = DoctorMessageDto.builder().build();
        if (messages != null && messages.size() > 0){
            messages.forEach(doctorMessage -> {
                String urlPart;
                if (Objects.equals(doctorMessage.getCategory(), Category.FATTEN_PIG_REMOVE.getKey())){
                    urlPart = "?groupId=";
                    msgDto.setListUrl("/group/list?farmId=" + doctorMessage.getFarmId() + "&searchFrom=MESSAGE");
                }else if (Objects.equals(doctorMessage.getCategory(), Category.STORAGE_SHORTAGE.getKey())){
                    urlPart = "?materialId=";
                }else {
                    urlPart = "?pigId=";
                    if (Objects.equals(doctorMessage.getCategory(), Category.BOAR_ELIMINATE.getKey())) {
                        msgDto.setListUrl("/boar/list?farmId=" + doctorMessage.getFarmId() + "&searchFrom=MESSAGE");
                    } else {
                        msgDto.setListUrl("/sow/list?farmId=" + doctorMessage.getFarmId() + "&searchFrom=MESSAGE");
                    }
                }
                 doctorMessage.setUrl(doctorMessage.getUrl().concat(urlPart + doctorMessage.getBusinessId() + "&farmId=" + doctorMessage.getFarmId()));
            });
        }
        msgDto.setPaging(paging);
        return msgDto;
    }

    /**
     * 查询系统消息分页
     * @param pageNo    页码
     * @param pageSize  页大小
     * @param criteria  参数
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
     * @return
     */
    @RequestMapping(value = "/noReadCount", method = RequestMethod.GET)
    public Long findNoReadCount() {
        if (!isUserLogin()) {
            return 0L;
        }
        return RespHelper.or500(doctorMessageReadService.findNoReadCount(UserUtil.getUserId()));
    }

    /**
     * 查询消息详情
     * @param id    消息id
     * @return
     */
    @RequestMapping(value = "/message/detail", method = RequestMethod.GET)
    public DoctorMessage findMessageDetail(@RequestParam("id") Long id) {
        DoctorMessage message = RespHelper.or500(doctorMessageReadService.findMessageById(id));
        if (message != null) {
            // 如果消息是未读, 将消息设置为已读
            if (Objects.equals(message.getStatus(), DoctorMessage.Status.NORMAL.getValue())) {
                message.setIsExpired(DoctorMessage.IsExpired.EXPIRED.getValue());
                message.setStatus(DoctorMessage.Status.READED.getValue());
                doctorMessageWriteService.updateMessage(message);
            }
            // 查询未读消息的数量
            //message.setNoReadCount(RespHelper.or500(doctorMessageReadService.findNoReadCount(UserUtil.getUserId())));
        }
        return message;
    }

    /**
     * 更新消息
     * @param doctorMessage
     * @return
     */
    @RequestMapping(value = "/message", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateMessage(@RequestBody DoctorMessage doctorMessage) {
        Preconditions.checkNotNull(doctorMessage, "message.not.null");
        if (doctorMessage.getId() == null) {
            doctorMessage.setCreatedBy(UserUtil.getUserId());
            RespHelper.or500(doctorMessageWriteService.createMessage(doctorMessage));
        }else {
            RespHelper.or500(doctorMessageWriteService.updateMessage(doctorMessage));
        }
        return Boolean.TRUE;
    }

    /**
     * 删除消息
     * @param id    消息id
     * @return
     */
    @RequestMapping(value  = "/message", method = RequestMethod.DELETE)
    public Boolean deleteMessage(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageWriteService.deleteMessageById(id));
    }


    /************************** 消息模板相关 **************************/

    /**
     * 查询系统消息模板列表
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
     * @param id
     * @return
     */
    @RequestMapping(value = "/template/detail", method = RequestMethod.GET)
    public DoctorMessageRuleTemplate getTemplateById(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(id));
    }

    /**
     * 创建或者更新模板
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
     * @param id
     * @return
     */
    @RequestMapping(value = "/template", method = RequestMethod.DELETE)
    public Boolean deleteTemplate(@RequestParam Long id) {
        return RespHelper.or500(doctorMessageRuleTemplateWriteService.deleteMessageRuleTemplateById(id));
    }

    /**
     * 获取猪场中消息规则与相应猪只数,列表
     * @return
     */
    @RequestMapping(value = "/warn/rule/list", method = RequestMethod.GET)
    public List<OneLevelMessageDto> getRulePigCountByFarmId(@RequestParam("farmId") Long farmId) {
        List<OneLevelMessageDto> list = Lists.newArrayList();
        List<DoctorMessageRule> doctorMessageRules = RespHelper.or500(doctorMessageRuleReadService.findMessageRulesByCriteria(ImmutableMap.of("farmId", farmId, "types", ImmutableList.of(DoctorMessageRuleTemplate.Type.WARNING, DoctorMessageRuleTemplate.Type.ERROR))));

        doctorMessageRules.forEach(doctorMessageRule -> {
            Integer pigCount = 0;
            if (Objects.equals(doctorMessageRule.getCategory(), Category.FATTEN_PIG_REMOVE.getKey())) {
                Map<String, Object> criteriaMap = Maps.newHashMap();
                criteriaMap.put("templateId", doctorMessageRule.getTemplateId());
                criteriaMap.put("farmId", doctorMessageRule.getFarmId());
                criteriaMap.put("isExpired", DoctorMessage.IsExpired.NOTEXPIRED.getValue());
                criteriaMap.put("userId", UserUtil.getCurrentUser().getId());
                criteriaMap.put("channel", Rule.Channel.SYSTEM.getValue());
                criteriaMap.put("statuses", ImmutableList.of(DoctorMessage.Status.NORMAL.getValue(), DoctorMessage.Status.READED.getValue()));
                List<DoctorMessage> messages = RespHelper.or500(doctorMessageReadService.findMessageByCriteria(criteriaMap));
                for (DoctorMessage doctorMessage : messages) {
                    try {
                        Map<String, Object> map = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(doctorMessage.getData(), JacksonType.MAP_OF_OBJECT);
                        pigCount += (Integer) map.get("quantity");
                    } catch (Exception e) {
                    }
                }

            } else if (!Objects.equals(doctorMessageRule.getCategory(), Category.STORAGE_SHORTAGE.getKey())) {
                Map<String, Object> criteriaMap = Maps.newHashMap();
                criteriaMap.put("templateId", doctorMessageRule.getTemplateId());
                criteriaMap.put("farmId", doctorMessageRule.getFarmId());
                criteriaMap.put("isExpired", DoctorMessage.IsExpired.NOTEXPIRED.getValue());
                criteriaMap.put("userId", UserUtil.getCurrentUser().getId());
                criteriaMap.put("channel", Rule.Channel.SYSTEM.getValue());
                criteriaMap.put("statuses", ImmutableList.of(DoctorMessage.Status.NORMAL.getValue(), DoctorMessage.Status.READED.getValue()));
                pigCount = RespHelper.or500(doctorMessageReadService.findMessageCountByCriteria(criteriaMap)).intValue();
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
     * @return
     *  如果登录: true
     *  否则:    false
     */
    private boolean isUserLogin() {
        return UserUtil.getCurrentUser() != null;
    }

}
