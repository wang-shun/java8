package io.terminus.doctor.web.admin.controller;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.user.service.DoctorAddressReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Desc: 基础数据后台人员操作controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/basic")
public class DoctorBasics {

    private final DoctorBasicReadService doctorBasicReadService;
    private final DoctorBasicWriteService doctorBasicWriteService;

    @RpcConsumer
    private DoctorAddressReadService doctorAddressReadService;

    @Autowired
    public DoctorBasics(DoctorBasicReadService doctorBasicReadService,
                        DoctorBasicWriteService doctorBasicWriteService) {
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBasicWriteService = doctorBasicWriteService;
    }

    /**
     * 根据基础数据类型和输入码查询基础数据
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    @RequestMapping(value = "/basic/type", method = RequestMethod.GET)
    public List<DoctorBasic> findBasicByTypeAndSrm(@RequestParam("type") Integer type,
                                                   @RequestParam(value = "srm", required = false) String srm) {
        return RespHelper.or500(doctorBasicReadService.findBasicByTypeAndSrm(type, srm));
    }

    /**
     * 创建或更新DoctorBasic
     * @return 是否成功
     */
    @RequestMapping(value = "/basic", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateBasic(@RequestBody DoctorBasic basic) {
        checkNotNull(basic, "basic.not.null");
        if (basic.getId() == null) {
            RespHelper.or500(doctorBasicWriteService.createBasic(basic));
        } else {
            basic.setUpdatorId(UserUtil.getUserId());
            basic.setUpdatorName(UserUtil.getCurrentUser().getName());
            RespHelper.or500(doctorBasicWriteService.updateBasic(basic));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorBasic(逻辑删除)
     * @return 是否成功
     */
    @RequestMapping(value = "/basic", method = RequestMethod.DELETE)
    public Boolean deleteBasic(@RequestParam("basicId") Long basicId) {
        return RespHelper.or500(doctorBasicWriteService.deleteBasicById(basicId));
    }

    /**
     * 获取地址树(取缓存到内存里的数据, 启动时加载)
     * @see io.terminus.doctor.user.service.DoctorAddressReadService
     *
     * @return 地址树
     */
    @RequestMapping(value = "/addressTree", method = RequestMethod.GET)
    public String findAddressTree() {
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(RespHelper.or500(doctorAddressReadService.findAllAddress()));
    }

    /**
     * 根据变动类型获取变动原因列表
     * @param changeTypeId
     * @return
     */
    @RequestMapping(value = "/queryChangeReasons", method = RequestMethod.GET)
    public List<DoctorChangeReason> queryChangeReasons(@RequestParam("changeTypeId") Long changeTypeId, @RequestParam(value = "srm", required = false) String srm){
        List<DoctorChangeReason> reasons =  RespHelper.or500(doctorBasicReadService.findChangeReasonByChangeTypeIdAndSrm(changeTypeId, srm));
        if (Arguments.isNullOrEmpty(reasons)){
            return Lists.newArrayList();
        }
        String changeTypeName = RespHelper.or500(doctorBasicReadService.findBasicById(changeTypeId)).getName();
        reasons.forEach(doctorChangeReason -> {
            try {
                Map<String, Object> extra;
                if (doctorChangeReason.getExtra() == null){
                    extra = Maps.newHashMap();
                }else {
                    extra = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(doctorChangeReason.getExtra(), JacksonType.MAP_OF_OBJECT);
                }
                extra.put("changeTypeName", changeTypeName);
                doctorChangeReason.setExtra(JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().writeValueAsString(extra));
            } catch (Exception e){

            }
        });
        return reasons;
    }

    /**
     * 修改和添加变动原因
     * @param doctorChangeReason
     * @return
     */
    @RequestMapping(value = "/update/changeReason", method = RequestMethod.POST)
    public Boolean updateAndCreateChangeReason(@RequestBody DoctorChangeReason doctorChangeReason){
        if (doctorChangeReason.getId() == null || RespHelper.or500(doctorBasicReadService.findChangeReasonById(doctorChangeReason.getId())) == null){
            RespHelper.or500(doctorBasicWriteService.createChangeReason(doctorChangeReason));
            return Boolean.TRUE;
        }
        return RespHelper.or500(doctorBasicWriteService.updateChangeReason(doctorChangeReason));
    }

    /**
     * 删除变动原因
     * @param changeReasonId
     * @return
     */
    @RequestMapping(value = "/delete/changeReason", method = RequestMethod.DELETE)
    public Boolean deleteChangeReason(@RequestParam("changeReasonId") Long changeReasonId){
        return RespHelper.or500(doctorBasicWriteService.deleteChangeReasonById(changeReasonId));
    }

    /**
     * 获取变动原因
     * @param changeReasonId
     * @return
     */
    @RequestMapping(value = "/find/changeReason", method = RequestMethod.GET)
    public DoctorChangeReason findChangeReason(@RequestParam("changeReasonId")Long changeReasonId){
        return RespHelper.or500(doctorBasicReadService.findChangeReasonById(changeReasonId));
    }

    /**
     * 分页查询变动类型
     * @param pageNo
     * @param pageSize
     * @param params
     * @return
     */
    @RequestMapping(value = "/paging/changeReason", method = RequestMethod.GET)
    public Paging<DoctorChangeReason> pagingChangeReason(@RequestParam(value = "pageNo", required = false) Integer pageNo, @RequestParam(value = "pageSize", required = false) Integer pageSize, @RequestParam Map<String, Object>params){
        params = Params.filterNullOrEmpty(params);
        return RespHelper.or500(doctorBasicReadService.pagingChangeReason(pageNo,pageSize,params));
    }
}
