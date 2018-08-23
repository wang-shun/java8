package io.terminus.doctor.web.admin.controller;

import com.google.api.client.util.Maps;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Joiners;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.user.model.*;
import io.terminus.doctor.user.service.*;
import io.terminus.doctor.web.admin.dto.DoctorGroupUserWithOrgAndFarm;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.parana.auth.model.CompiledTree;
import io.terminus.parana.common.utils.EncryptUtil;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.parana.common.utils.RespHelper.or500;

/**
 * Desc: admin分配权限的api
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/17
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/group-user")
public class DoctorAdminUsers {

    @RpcConsumer
    private DoctorUserReadService doctorUserReadService;
    @RpcConsumer
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    @RpcConsumer
    private DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private UserWriteService<User> userUserWriteService;
    @RpcConsumer
    private DoctorServiceStatusReadService doctorServiceStatusReadService;
    @RpcConsumer
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    @RpcConsumer
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @RpcConsumer
    private DoctorServiceReviewReadService doctorServiceReviewReadService;
    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;
    @RpcConsumer
    private DoctorBarnReadService doctorBarnReadService;
    @RpcConsumer
    private PrimaryUserReadService primaryUserReadService;
    @RpcConsumer
    private SubRoleWriteService subRoleWriteService;
    @RpcConsumer
    private SubRoleReadService subRoleReadService;
    @RpcConsumer
    private PrimaryUserWriteService primaryUserWriteService;

    /**
     * 新增集团用户
     * @param mobile    手机号
     * @param name      登录名
     * @param password  密码  默认手机号
     * @return 用户id
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Long addGroupUser(@RequestParam String mobile,
                             @RequestParam(required = false) String name,
                             @RequestParam(required = false) String realName,
                             @RequestParam(required = false) String password) {
        User user = checkGroupUser(mobile, name, realName, password);

        Long userId;
        if (isNull(user.getId())) {
            userId = RespHelper.or500(userUserWriteService.create(user));
        } else {
            RespHelper.or500(userUserWriteService.update(user));
            userId = user.getId();
        }
        initDefaultServiceStatus(userId, user.getMobile(), user.getName());
        return userId;
    }

    //初始化审核服务
    private void initDefaultServiceStatus(Long userId, String mobile, String realName) {
        DoctorServiceStatus doctorServiceStatus = RespHelper.or500(doctorServiceStatusReadService.findByUserId(userId));
        if(Arguments.isNull(doctorServiceStatus)) {
            DoctorServiceStatus status = new DoctorServiceStatus();
            status.setUserId(userId);
            //猪场软件初始状态
            status.setPigdoctorReviewStatus(DoctorServiceReview.Status.OK.getValue());
            status.setPigdoctorStatus(DoctorServiceStatus.Status.OPENED.value());
            //电商初始状态
            status.setPigmallStatus(DoctorServiceStatus.Status.BETA.value());
            status.setPigmallReason("敬请期待");
            status.setPigmallReviewStatus(DoctorServiceReview.Status.INIT.getValue());
            //大数据初始状态
            status.setNeverestStatus(DoctorServiceStatus.Status.BETA.value());
            status.setNeverestReason("敬请期待");
            status.setNeverestReviewStatus(DoctorServiceReview.Status.INIT.getValue());
            //生猪交易初始状态
            status.setPigtradeStatus(DoctorServiceStatus.Status.BETA.value());
            status.setPigtradeReason("敬请期待");
            status.setPigtradeReviewStatus(DoctorServiceReview.Status.INIT.getValue());
            RespHelper.or500(doctorServiceStatusWriteService.createServiceStatus(status));
        }else {
            doctorServiceStatus.setPigdoctorReviewStatus(DoctorServiceReview.Status.OK.getValue());
            doctorServiceStatus.setPigdoctorStatus(DoctorServiceStatus.Status.OPENED.value());
            RespHelper.or500(doctorServiceStatusWriteService.updateServiceStatus(doctorServiceStatus));
        }

        DoctorServiceReview doctorReview = RespHelper.or500(doctorServiceReviewReadService.findServiceReviewByUserIdAndType(userId, DoctorServiceReview.Type.PIG_DOCTOR));
        if( doctorReview == null) {
            RespHelper.or500(doctorServiceReviewWriteService.initServiceReview(userId, mobile, realName));
        }
        DoctorServiceReview doctorReviewNew = RespHelper.or500(doctorServiceReviewReadService.findServiceReviewByUserIdAndType(userId, DoctorServiceReview.Type.PIG_DOCTOR));
        if (doctorReviewNew != null) {
            doctorReviewNew.setStatus(DoctorServiceReview.Status.OK.getValue());
            RespHelper.or500(doctorServiceReviewWriteService.updateReview(doctorReviewNew));
        }
    }

    //拼接集团用户数据
    private User checkGroupUser(String mobile, String name, String realName, String password) {
//        Response<Boolean> mobileResponse = doctorUserReadService.checkExist(mobile, LoginType.MOBILE);
//        if(mobileResponse.isSuccess() && mobileResponse.getResult()){
//            log.error("user existed, user:{}", mobileResponse.getResult());
//            throw new JsonResponseException("duplicated.mobile");
//        }
//        Response<Boolean> nameResponse = doctorUserReadService.checkExist(name, LoginType.NAME);
//        if(nameResponse.isSuccess() && nameResponse.getResult()){
//            log.error("user existed, user:{}", nameResponse.getResult());
//            throw new JsonResponseException("duplicated.name");
//        }

        RespHelper.or500(doctorUserReadService.checkExist(mobile, name));
        User user;
        password = StringUtils.hasText(password) ? password : mobile;
        Response<User> mobileResponse = doctorUserReadService.findBy(mobile, LoginType.MOBILE);
        if (mobileResponse.isSuccess() && notNull(mobileResponse.getResult())) {
            user = mobileResponse.getResult();
            if (StringUtils.hasText(password)) {  //对密码加盐加密
                password = EncryptUtil.encrypt(password);
            }
        } else {
            user = new User();
        }
        user.setMobile(mobile);
        user.setName(name);
        Map<String, String> extra = MoreObjects.firstNonNull(user.getExtra(), Maps.newHashMap());
        extra.put("realName", realName);

        //密码默认为手机号
        user.setPassword(password);
        user.setType(UserType.FARM_ADMIN_PRIMARY.value());
        user.setStatus(UserStatus.NORMAL.value());
        user.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));
        return user;
    }

    /**
     * 设置集团用户权限(新建与编辑)
     */
    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public Boolean groupUserAuth(@RequestParam Long userId,
                                 @RequestParam String orgIds,
                                 @RequestParam(required = false) String farmIds) {
        User user = RespHelper.or500(doctorUserReadService.findById(userId));
        if (notNull(user)&&notNull(user.getExtra()) && user.getExtra().containsKey("frozen")
                && user.getExtra().get("frozen").equals(IsOrNot.YES.getKey().toString())) {
            log.error("admin add user auth, userId:({}) user is frozen", userId);
            throw new JsonResponseException("user.is.frozen");
        }
        if (user == null) {
            log.error("admin add user auth, userId:({}) not found", userId);
            throw new JsonResponseException("user.not.found");
        }
        initDefaultServiceStatus(userId, user.getMobile(), user.getName());
        DoctorUserDataPermission permission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
        if (permission == null) {
            permission = getPermission(new DoctorUserDataPermission(), userId, orgIds, farmIds);
            RespHelper.or500(doctorUserDataPermissionWriteService.createDataPermission(permission));
            return true;
        }
        return RespHelper.or500(doctorUserDataPermissionWriteService.updateDataPermission(getPermission(permission, userId, orgIds, farmIds)));
    }

    private DoctorUserDataPermission getPermission(DoctorUserDataPermission permission, Long userId, String orgIds, String farmIds) {
        permission.setUserId(userId);
        permission.setOrgIds(orgIds);
        permission.setFarmIds(notEmpty(farmIds) ? farmIds : getFarmIds(Splitters.splitToLong(orgIds, Splitters.COMMA)));
        permission.setBarnIds(notEmpty(permission.getFarmIds()) ? getBarnIds(Splitters.splitToLong(permission.getFarmIds(), Splitters.COMMA)) : null);
        return permission;
    }

    private String getBarnIds(List<Long> farmIds) {
        List<Long> barnIds = Lists.newArrayList();
        farmIds.forEach(farmId -> {
            List<DoctorBarn> barns = RespHelper.or500(doctorBarnReadService.findBarnsByFarmId(farmId));
            barnIds.addAll(barns.stream().map(DoctorBarn::getId).collect(Collectors.toList()));
        });
        return Joiners.COMMA.join(barnIds);
    }


    private String getFarmIds(List<Long> orgIds) {
        List<Long> farmIds = Lists.newArrayList();
        for (Long orgId : orgIds) {
            List<DoctorFarm> farms = RespHelper.or500(doctorFarmReadService.findFarmsByOrgId(orgId));
            farmIds.addAll(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()));
        }
        return Joiners.COMMA.join(farmIds);
    }


    /**
     * 分页集团用户搭配权限
     *
     * @param id          用户的id
     * @param name        用户的名字
     * @param email       用户邮箱
     * @param mobile      用户手机号
     * @param status      用户状态
     * @param type        用户类型
     * @param pageNo      当前页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<DoctorGroupUserWithOrgAndFarm> pagingGroupUser(@RequestParam(required = false) Long id,
                                                                 @RequestParam(required = false) String name,
                                                                 @RequestParam(required = false) String email,
                                                                 @RequestParam(required = false) String mobile,
                                                                 @RequestParam(required = false) Integer status,
                                                                 @RequestParam(required = false) Integer type,
                                                                 @RequestParam(required = false) Integer pageNo,
                                                                 @RequestParam(required = false) Integer pageSize) {
        if (Strings.isNullOrEmpty(name)) {
            name = null;
        }
        if (Strings.isNullOrEmpty(email)) {
            email = null;
        }
        if (Strings.isNullOrEmpty(mobile)) {
            mobile = null;
        }
        Paging<User> userPaging = RespHelper.or500(primaryUserReadService.pagingOpenDoctorServiceUser(id, name, email, mobile, status, type, pageNo, pageSize));
        if (userPaging.getTotal() == 0L) {
            return new Paging<>(0L, Collections.emptyList());
        }
        return new Paging<>(userPaging.getTotal(), withOrgAndFarm(userPaging.getData()));
    }

    //带上用户权限公司和猪场的分页
    private List<DoctorGroupUserWithOrgAndFarm> withOrgAndFarm(List<User> users) {
        Map<Long, DoctorOrg> orgMap = RespHelper.or500(doctorOrgReadService.findAllOrgs()).stream()
                .collect(Collectors.toMap(DoctorOrg::getId, o -> o));

        Map<Long, DoctorFarm> farmMap = RespHelper.or500(doctorFarmReadService.findAllFarms()).stream()
                .collect(Collectors.toMap(DoctorFarm::getId, f -> f));

        return users.stream()
                .map(user -> {
                    DoctorGroupUserWithOrgAndFarm groupUser = BeanMapper.map(user, DoctorGroupUserWithOrgAndFarm.class);
                    Response<DoctorUserDataPermission> permissionResponse = doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId());

                    if (permissionResponse.isSuccess() && permissionResponse.getResult() != null) {
                        DoctorUserDataPermission permission = permissionResponse.getResult();

                        //设置一下权限
                        groupUser.setOrgs(mapIdToList(permission.getOrgIdsList(), orgMap));
                        groupUser.setFarms(mapIdToList(permission.getFarmIdsList(), farmMap));
                    }
                    return groupUser;
                })
                .collect(Collectors.toList());
    }

    private <T> List<T> mapIdToList(List<Long> ids, Map<Long, T> map) {
        if (!notEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(map::get).collect(Collectors.toList());
    }


    //孔景军(增加主账号权限)
    @RequestMapping(value = "/getTree", method = RequestMethod.GET)
    public String getTree(){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CompiledTree> result= restTemplate.getForEntity("https://pig.xrnm.com/api/auth/tree?role=SUB",  CompiledTree.class, ImmutableMap.of( "role", "SUB"));
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(result.getBody());
    }


    /**
     * 创建账号角色
     *
     * @param role 运营角色
     * @return 角色主键 ID
     */
    @RequestMapping(value = "/addRole", method = RequestMethod.GET)
    public Long createRole(@RequestBody SubRole role) {
        role.setAppKey("PC");
        role.setStatus(1);
        return or500(subRoleWriteService.createRole(role));
    }

    /**
     * 更新子账号角色
     *
     * @param role 角色授权内容
     * @return 是否更新成功
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Boolean updateRole(@RequestBody SubRole role) {
        if(role.getId() == null){
            throw new JsonResponseException(500, "sub.role.id.miss");
        }

        SubRole existRole = io.terminus.parana.common.utils.RespHelper.orServEx(subRoleReadService.findById(role.getId()));
        if (existRole == null) {
            throw new JsonResponseException(500, "sub.role.not.exist");
        }

        role.setId(role.getId());
        role.setAppKey(null); // prevent update
        or500(subRoleWriteService.updateRole(role));

        //如果角色名称发生了改变, 则更新冗余字段
        if(!Objects.equals(existRole.getName(), role.getName())){
            or500(primaryUserWriteService.updateRoleName(role.getId(), role.getName()));
        }
        return true;
    }
}
