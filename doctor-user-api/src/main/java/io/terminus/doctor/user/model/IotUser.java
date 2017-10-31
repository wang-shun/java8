package io.terminus.doctor.user.model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/10/11.
 * 物联网运营用户与角色管理表
 */
@Data
public class IotUser implements Serializable{

    private static final long serialVersionUID = -6956491801378637479L;

    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户真实姓名
     */
    private String userRealName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 物联网角色id
     */
    private Long iotRoleId;

    /**
     * 物联网角色名
     */
    private String iotRoleName;

    /**
     * 物联网运营用户类型
     * @see TYPE
     */
    private Integer type;

    /**
     * 用户装太
     * @see io.terminus.doctor.user.model.Sub.Status
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    public enum TYPE {
        IOT_ADMIN(1, "物联网运营主账户"),
        IOT_OPERATOR(2, "物联网运营子账户");

        @Getter
        private int value;
        @Getter
        private String desc;

        TYPE(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static IotUser.TYPE from(int number) {
            return Lists.newArrayList(IotUser.TYPE.values()).stream()
                    .filter(s -> Objects.equal(s.value, number))
                    .findFirst()
                    .<ServiceException>orElseThrow(() -> {
                        throw new ServiceException("iot.user.role.type.error");
                    });
        }
    }
}
