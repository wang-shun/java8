/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.common.model;

import io.terminus.common.model.BaseUser;
import lombok.Data;

import java.util.List;

/**
 * parana用户
 *
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-15
 */
@Data
public class ParanaUser implements BaseUser {
    private static final long serialVersionUID = -2961193418926377287L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 用户类型,0:管理员, 1: 买家, 2: 卖家
     */
    private Integer type;

    /**
     * 店铺id, 只有卖家才有这个信息
     */
    private Long shopId;

    /**
     * 用户所有的角色列表
     */
    private List<String> roles;


    /**
     * 获取用户类型名称 含义由子类自行定义
     *
     * @return 用户类型名称
     */
    public String getTypeName() {
        return null;
    }
}
