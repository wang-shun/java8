package io.terminus.doctor.web.admin.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Params;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorDepartmentReadService;
import io.terminus.doctor.user.service.DoctorDepartmentWriteService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Desc: admin端 公司api
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/org")
public class DoctorAdminOrgs {

    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;
    @RpcConsumer
    private DoctorDepartmentReadService doctorDepartmentReadService;
    @RpcConsumer
    private DoctorDepartmentWriteService doctorDepartmentWriteService;

    /**
     * 查询全部公司
     * @return 全部公司
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorOrg> findAllOrgs() {
        return RespHelper.or500(doctorOrgReadService.findAllOrgs());
    }

    /**
     * suggest公司
     * @param fuzzyName 公司名
     * @param type 公司类型
     * @see io.terminus.doctor.user.model.DoctorOrg.Type
     * @return 公司列表
     */
    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    public List<DoctorOrg> suggestOrg(@RequestParam String fuzzyName, @RequestParam Integer type) {
        return RespHelper.or500(doctorOrgReadService.suggestOrg(fuzzyName, type));
    }

    /**
     * 分页查询集团
     * @param pageNo 页码
     * @param pageSize 分页大小
     * @return 集团
     */
    @RequestMapping(value = "/paging/clique", method = RequestMethod.GET)
    public Paging<DoctorOrg> pagingClique(@RequestParam(required = false) Integer pageNo,
                                          @RequestParam(required = false) Integer pageSize,
                                          @RequestParam Map<String, Object> params) {
        params = Params.filterNullOrEmpty(params);
        params.put("type", DoctorOrg.Type.CLIQUE.getValue());
        return RespHelper.or500(doctorOrgReadService.paging(params, pageSize, pageNo));
    }

    /**
     * 获取部门结构树
     * @param orgId 根节点
     * @return 集团数
     */
    @RequestMapping(value = "/clique/tree", method = RequestMethod.GET)
    public DoctorDepartmentDto findCliqueTree(@RequestParam Long orgId) {
        return RespHelper.or500(doctorDepartmentReadService.findCliqueTree(orgId));
    }

    /**
     * 绑定部门关系
     * @param parentId 父节点id
     * @param orgIds 子节点列表
     * @return
     */
    @RequestMapping(value = "/bind", method = RequestMethod.PUT)
    public Boolean bindDepartment(@RequestParam Long parentId, @RequestParam String orgIds) {
        List<Long> orgIdList = Splitters.splitToLong(orgIds, Splitters.UNDERSCORE);
        return RespHelper.or500(doctorDepartmentWriteService.bindDepartment(parentId, orgIdList));
    }

    /**
     * 解绑部门关系
     * @param orgId 节点id
     * @return
     */
    @RequestMapping(value = "/unbind", method = RequestMethod.PUT)
    public Boolean unbindDepartment(@RequestParam Long orgId) {
        return RespHelper.or500(doctorDepartmentWriteService.unbindDepartment(orgId));
    }

    /**
     * 可绑定的在次公司下的公司列表
     * @param orgId 父公司id
     * @return 公司列表
     */
    @RequestMapping(value = "/available/bind", method = RequestMethod.GET)
    public List<DoctorDepartmentDto> availableBindDepartment(@RequestParam Long orgId) {
        return RespHelper.or500(doctorDepartmentReadService.availableBindDepartment(orgId));
    }
}
