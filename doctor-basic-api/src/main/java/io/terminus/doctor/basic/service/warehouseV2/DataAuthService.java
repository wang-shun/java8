package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.DataAuth;

import java.util.Map;

/**
 * @ClassName DataAuthService
 * @Description TODO
 * @Author Danny
 * @Date 2018/8/24 17:03
 */
public interface DataAuthService {

    Response selGroups();

    Response selOrgs(Integer groupId);

    Response selFarms(Integer orgId);

    Response getUserRoleInfo(Map<String,String> params);

    Response userSingleRoleInfo(Integer userId);

    Response editUserRoleInfo(Map<String,String> params);

    Response getDataSubRoles(Integer userId);

    Response saveDataSubRoles(DataAuth dataSubRoles);

    Response changeUserPassword(Map<String,String> params);
}
