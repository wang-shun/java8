package io.terminus.doctor.user.dto;

import io.terminus.doctor.user.model.IotUserRole;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/10/11.
 */
@Data
public class IotUserRoleInfo extends IotUserRole implements Serializable{
    private static final long serialVersionUID = 5182226909773266561L;

    private String realName;
}
