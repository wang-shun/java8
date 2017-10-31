package io.terminus.doctor.user.dto;

import io.terminus.doctor.user.model.IotUser;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/10/16.
 */
@Data
public class IotUserDto extends IotUser implements Serializable{
    private static final long serialVersionUID = -7325715566527250832L;

    private String password;
}
