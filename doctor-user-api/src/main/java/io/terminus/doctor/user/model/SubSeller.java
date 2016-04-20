package io.terminus.doctor.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.terminus.common.utils.JsonMapper;
import io.terminus.parana.common.constants.JacksonType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 个人卖家外部用户
 *
 * @author Effet
 */
@Data
public class SubSeller implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.nonEmptyMapper().getMapper();

    /**
     * 主键
     */
    private Long id;

    /**
     * 被授权用户 ID
     */
    private Long userId;

    /**
     * 用户名 (冗余)
     */
    private String userName;

    /**
     * 授权的店铺 ID
     */
    private Long shopId;

    /**
     * 授权状态:
     *
     * 0. 未生效(冻结), 1. 生效, -1. 删除
     */
    private Integer status;

    /**
     * 角色 ID 列表 {@link io.terminus.doctor.user.model.SellerRole}
     *
     * 不存数据库
     */
    @Setter(AccessLevel.NONE)
    private List<SubSellerRole> roles;

    /**
     * 角色 ID 列表, JSON 字符串不存数据库
     */
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String rolesJson;

    /**
     * 扩展信息, 不存数据库
     */
    @Setter(AccessLevel.NONE)
    private Map<String, String> extra;

    /**
     * 扩展信息 JSON, 存数据库
     */
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String extraJson;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @SneakyThrows
    public void setRoles(List<SubSellerRole> roles) {
        this.roles = roles;
        if (roles == null) {
            this.rolesJson = null;
        } else {
            this.rolesJson = OBJECT_MAPPER.writeValueAsString(roles);
        }
    }

    @SneakyThrows
    public void setRolesJson(String rolesJson) {
        this.rolesJson = rolesJson;
        if (Strings.isNullOrEmpty(rolesJson)) {
            this.roles = Collections.emptyList();
        } else {
            this.roles = OBJECT_MAPPER.readValue(rolesJson, new TypeReference<List<SubSellerRole>>() {
            });
        }
    }

    @SneakyThrows
    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
        if (extra == null || extra.isEmpty()) {
            this.extraJson = null;
        } else {
            this.extraJson = OBJECT_MAPPER.writeValueAsString(extra);
        }
    }

    @SneakyThrows
    public void setExtraJson(String extraJson) {
        this.extraJson = extraJson;
        if (Strings.isNullOrEmpty(extraJson)) {
            this.extra = Collections.emptyMap();
        } else {
            this.extra = OBJECT_MAPPER.readValue(extraJson, JacksonType.MAP_OF_STRING);
        }
    }

    @Data
    public static class SubSellerRole implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;

        private String name;
    }
}
