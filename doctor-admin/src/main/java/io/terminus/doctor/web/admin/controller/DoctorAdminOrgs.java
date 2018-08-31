package io.terminus.doctor.web.admin.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Params;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.service.*;
import io.terminus.doctor.web.admin.dto.DoctorAvailableBindDto;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;

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
    private DoctorOrgWriteService doctorOrgWriteService;
    @RpcConsumer
    private DoctorDepartmentReadService doctorDepartmentReadService;
    @RpcConsumer
    private DoctorDepartmentWriteService doctorDepartmentWriteService;
    @RpcConsumer
    private DoctorUserReadService doctorUserReadService;
    @RpcConsumer
    private DoctorServiceStatusReadService doctorServiceStatusReadService;

    //添加公司
    @RequestMapping(value = "/addOrg", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long addOrg(@RequestBody DoctorOrg org) {
        org.setParentId(0L);
        Response<Long> response = doctorOrgWriteService.createOrg(org);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 查询全部公司
     * @return 全部公司
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorOrg> findAllOrgs() {
        return RespHelper.or500(doctorOrgReadService.findAllOrgs());
    }

    /**
     * 查询全部开通猪场软件服务的公司
     * @return 公司列表
     */
    @RequestMapping(value = "/all/open/service", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorOrg> findAllOpenDoctortOrgs() {
        List<DoctorOrg> orgs =  RespHelper.or500(doctorOrgReadService.findAllOrgs());

        return orgs.stream().filter(doctorOrg -> {
            Response<User> userResponse = doctorUserReadService.findBy(doctorOrg.getMobile(), LoginType.MOBILE);
            if (!userResponse.isSuccess()) {
                return false;
            }

            DoctorServiceStatus doctorServiceStatus = RespHelper.or500(doctorServiceStatusReadService.findByUserId(userResponse.getResult().getId()));
            return notNull(doctorServiceStatus) && Objects.equals(doctorServiceStatus.getPigdoctorStatus(), DoctorServiceStatus.Status.OPENED.value());
        }).collect(Collectors.toList());
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
    public Paging<DoctorDepartmentDto> pagingClique(@RequestParam(required = false) Integer pageNo,
                                          @RequestParam(required = false) Integer pageSize,
                                          @RequestParam Map<String, Object> params) {
        params = Params.filterNullOrEmpty(params);
//        params.put("type", DoctorOrg.Type.CLIQUE.getValue());
        if(isNull(params)||params.size()==0||params.equals("")){
            params.put("type", DoctorOrg.Type.CLIQUE.getValue());
        }
        return RespHelper.or500(doctorDepartmentReadService.pagingCliqueTree(params, pageSize, pageNo));
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
    public DoctorAvailableBindDto availableBindDepartment(@RequestParam Long orgId,@RequestParam(required = false) String name) {
        DoctorAvailableBindDto dto = new DoctorAvailableBindDto();
        dto.setDepartmentDtoList(RespHelper.or500(doctorDepartmentReadService.availableBindDepartment(orgId,name)));
        dto.setDoctorDepartmentDto(RespHelper.or500(doctorDepartmentReadService.findCliqueTree(orgId)));
        return dto;
    }

    /**
     * 员工查询(yusq)
     */
    @RequestMapping(value = "/staffQuery", method = RequestMethod.POST)
    public Response staffQuery(@RequestBody(required = false) Map<String, Object> params){
        return  doctorOrgReadService.staffQuery(params);
    }
}
