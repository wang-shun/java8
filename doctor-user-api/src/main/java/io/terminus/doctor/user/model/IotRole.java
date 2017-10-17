package io.terminus.doctor.user.model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by xjn on 17/10/11.
 * 物联网运营角色表
 */
@Data
public class IotRole implements Serializable{
    private static final long serialVersionUID = -6001889391702598985L;

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
     * 角色状态:
     *
     * 0. 未生效(冻结), 1. 生效, -1. 删除
     * @see Status
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
//    @JsonIgnore
    private String allowJson;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    public String getBaseRole() {
        return "IOT";
    }

    public enum Status {
        FROZEN(0, "未生效"),
        EFFECTED(1, "生效"),
        DELETED(-1, "删除");

        @Getter
        private int value;
        @Getter
        private String desc;

        Status(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Status from(int number) {
            return Lists.newArrayList(Status.values()).stream()
                    .filter(s -> Objects.equal(s.value, number))
                    .findFirst()
                    .<ServiceException>orElseThrow(() -> {
                        throw new ServiceException("doctor.service.review.status.error");
                    });
        }
    }
}
