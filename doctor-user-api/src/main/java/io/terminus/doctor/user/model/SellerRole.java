package io.terminus.doctor.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.terminus.common.utils.JsonMapper;
import io.terminus.parana.common.constants.JacksonType;
import io.terminus.parana.user.auth.CustomRole;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 卖家细分权限
 *
 * @author Effet
 */
@Data
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "name", "desc", "shopId", "status"})
public class SellerRole implements Serializable, CustomRole {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.nonEmptyMapper().getMapper();

    /**
     * 主键
     */
    private Long id;

    /**
     * 角色名
     */
    private String name;

    /**
     * 角色描述
     */
    private String desc;

    /**
     * 店铺 ID
     */
    private Long shopId;

    /**
     * 角色所属 (PC / MOBILE)
     */
    private String appKey;

    /**
     * 角色状态:
     *
     * 0. 未生效(冻结), 1. 生效, -1. 删除
     */
    private Integer status;

    /**
     * 角色对应资源列表, 不存数据库
     */
    @Setter(AccessLevel.NONE)
    private List<String> allow;

    /**
     * 角色对应资源列表 JSON, 存数据库
     */
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private String allowJson;

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
    public void setAllow(List<String> allow) {
        this.allow = allow;
        if (allow == null) {
            this.allowJson = null;
        } else {
            this.allowJson = OBJECT_MAPPER.writeValueAsString(allow);
        }
    }

    @SneakyThrows
    public void setAllowJson(String allowJson) {
        this.allowJson = allowJson;
        if (allowJson == null) {
            this.allow = null;
        } else if (allowJson.length() == 0) {
            this.allow = Collections.emptyList();
        } else {
            this.allow = OBJECT_MAPPER.readValue(allowJson, new TypeReference<List<String>>() {
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

    @Override
    public boolean isActive() {
        return Objects.equals(status, 1);
    }

    @Override
    public Map<String, String> getContext() {
        Map<String, String> context = new HashMap<>();
        if (shopId != null) {
            context.put("shopId", String.valueOf(shopId));
        }
        return context;
    }
}
