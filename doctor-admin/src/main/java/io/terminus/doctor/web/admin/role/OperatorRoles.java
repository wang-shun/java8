package io.terminus.doctor.web.admin.role;

import io.terminus.common.exception.JsonResponseException;

import io.terminus.doctor.user.model.OperatorRole;
import io.terminus.doctor.user.service.OperatorRoleReadService;
import io.terminus.doctor.user.service.OperatorRoleWriteService;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.parana.common.utils.RespHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.terminus.parana.common.utils.RespHelper.or500;

/**
 * @author Effet
 */
@RestController
@RequestMapping("/api/operator/role")
public class OperatorRoles {

    private final OperatorRoleReadService operatorRoleReadService;

    private final OperatorRoleWriteService operatorRoleWriteService;

    @Autowired
    public OperatorRoles(OperatorRoleReadService OperatorRoleReadService, OperatorRoleWriteService OperatorRoleWriteService) {
        this.operatorRoleReadService = OperatorRoleReadService;
        this.operatorRoleWriteService = OperatorRoleWriteService;
    }

    /**
     * 创建运营角色
     *
     * @param role 运营角色
     * @return 角色主键 ID
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Long createRole(@RequestBody OperatorRole role) {
        role.setAppKey(ThreadVars.getAppKey());
        role.setStatus(1);
        return or500(operatorRoleWriteService.createRole(role));
    }

    /**
     * 更新运营角色
     *
     * @param id   角色 ID
     * @param role 角色授权内容
     * @return 是否更新成功
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Boolean updateRole(@PathVariable Long id, @RequestBody OperatorRole role) {
        OperatorRole existRole = RespHelper.orServEx(operatorRoleReadService.findById(id));
        if (existRole == null) {
            throw new JsonResponseException(500, "operator.role.not.exist");
        }
        role.setId(id);
        role.setAppKey(null); // prevent update
        role.setStatus(null); // prevent update
        return or500(operatorRoleWriteService.updateRole(role));
    }

    /**
     * 拿所有合法角色
     *
     * @return 角色列表
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<OperatorRole> findAllRoles() {
        return RespHelper.or500(operatorRoleReadService.findByStatus(ThreadVars.getAppKey(), 1));
    }
}
