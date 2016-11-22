package io.terminus.doctor.web.front.basic.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.service.DoctorAddressReadService;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Desc: 基础数据Controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/basic")
public class DoctorBasics {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorBasicReadService doctorBasicReadService;
    private final DoctorBasicWriteService doctorBasicWriteService;
    private final DoctorFarmAuthCenter doctorFarmAuthCenter;

    @RpcConsumer
    private DoctorAddressReadService doctorAddressReadService;

    @Autowired
    public DoctorBasics(DoctorBasicReadService doctorBasicReadService,
                        DoctorBasicWriteService doctorBasicWriteService,
                        DoctorFarmAuthCenter doctorFarmAuthCenter) {
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBasicWriteService = doctorBasicWriteService;
        this.doctorFarmAuthCenter = doctorFarmAuthCenter;
    }

    /**
     * 根据id查询基础数据表
     * @param basicId 主键id
     * @return 基础数据表
     */
    @RequestMapping(value = "/id", method = RequestMethod.GET)
    public DoctorBasic findBasicById(@RequestParam("farmId") Long farmId,
                                     @RequestParam("basicId") Long basicId) {
        return RespHelper.or500(doctorBasicReadService.findBasicByIdFilterByFarmId(farmId, basicId));
    }

    /**
     * 根据基础数据类型和输入码查询基础数据(缓存)
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    @RequestMapping(value = "/type", method = RequestMethod.GET)
    public List<DoctorBasic> findBasicByTypeAndSrmWithCache(@RequestParam("farmId") Long farmId,
                                                            @RequestParam("type") Integer type,
                                                            @RequestParam(value = "srm", required = false) String srm) {
        return RespHelper.or500(doctorBasicReadService.findBasicByTypeAndSrmWithCacheFilterByFarmId(farmId, type, srm));
    }

    /************************** 猪群变动相关 **************************/
    /**
     * 根据id查询变动原因表
     * @param changeReasonId 主键id
     * @return 变动原因表
     */
    @RequestMapping(value = "/changeReason/id", method = RequestMethod.GET)
    public DoctorChangeReason findChangeReasonById(@RequestParam("farmId") Long farmId,
                                                   @RequestParam("changeReasonId") Long changeReasonId) {
        return RespHelper.or500(doctorBasicReadService.findChangeReasonByIdFilterByFarmId(farmId, changeReasonId));
    }

    /**
     * 根据变动类型和输入码查询
     * @param changeTypeId 变动类型id
     * @param srm 不区分大小写模糊匹配
     * @return 变动原因列表
     */
    @RequestMapping(value = "/changeReason/typeId", method = RequestMethod.GET)
    public List<DoctorChangeReason> findChangeReasonByChangeTypeIdAndSrm(@RequestParam("farmId") Long farmId,
                                                                         @RequestParam("changeTypeId") Long changeTypeId,
                                                                         @RequestParam(required = false) String srm) {
        return RespHelper.or500(doctorBasicReadService.findChangeReasonByChangeTypeIdAndSrmFilterByFarmId(farmId, changeTypeId, srm));
    }

    /*********************** 猪场客户相关 ************************/
    /**
     * 根据id查询客户表
     * @param customerId 主键id
     * @return 客户表
     */
    @RequestMapping(value = "/customer/id", method = RequestMethod.GET)
    public DoctorCustomer findCustomerById(@RequestParam("customerId") Long customerId) {
        return RespHelper.or500(doctorBasicReadService.findCustomerById(customerId));
    }

    /**
     * 根据farmId查询客户表
     * @param farmId 猪场id
     * @return 客户表列表
     */
    @RequestMapping(value = "/customer/farmId", method = RequestMethod.GET)
    public List<DoctorCustomer> findCustomersByfarmId(@RequestParam("farmId") Long farmId) {
        return RespHelper.or500(doctorBasicReadService.findCustomersByFarmId(farmId));
    }

    /**
     * 创建或更新DoctorCustomer
     * @return 是否成功
     */
    @RequestMapping(value = "/customer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createOrUpdateCustomer(@RequestBody DoctorCustomer customer) {
        checkNotNull(customer, "customer.not.null");

        //权限中心校验权限
        doctorFarmAuthCenter.checkFarmAuth(customer.getFarmId());

        if (customer.getId() == null) {
            customer.setCreatorId(UserUtil.getUserId());
            customer.setCreatorName(UserUtil.getCurrentUser().getName());
            RespHelper.or500(doctorBasicWriteService.createCustomer(customer));
        } else {
            customer.setUpdatorId(UserUtil.getUserId());
            customer.setUpdatorName(UserUtil.getCurrentUser().getName());
            RespHelper.or500(doctorBasicWriteService.updateCustomer(customer));
        }
        return customer.getId();
    }

    /**
     * 根据主键id删除DoctorCustomer
     * @return 是否成功
     */
    @RequestMapping(value = "/customer", method = RequestMethod.DELETE)
    public Boolean deleteCustomer(@RequestParam("customerId") Long customerId) {
        DoctorCustomer customer = RespHelper.or500(doctorBasicReadService.findCustomerById(customerId));

        //权限中心校验权限
        doctorFarmAuthCenter.checkFarmAuth(customer.getFarmId());

        return RespHelper.or500(doctorBasicWriteService.deleteCustomerById(customerId));
    }

    /**
     * 获取地址树(取缓存到内存里的数据, 启动时加载)
     * @see io.terminus.doctor.user.service.DoctorAddressReadService
     *
     * @return 地址树
     */
    @RequestMapping(value = "/addressTree", method = RequestMethod.GET)
    public String findAddressTree() {
        return JSON_MAPPER.toJson(RespHelper.or500(doctorAddressReadService.findAllAddress()));
    }
}
