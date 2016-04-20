/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.controller;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.user.address.model.ReceiveAddress;
import io.terminus.parana.user.address.service.ReceiveAddressReadService;
import io.terminus.parana.user.address.service.ReceiveAddressWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收货地址信息
 *
 * Author  : panxin
 * Date    : 12:14 AM 3/6/16
 * Mail    : panxin@terminus.io
 */
@Slf4j
@RestController
@RequestMapping("/api/user/receiveAddress")
public class ReceiveAddresses {

    private final ReceiveAddressReadService receiveAddressReadService;

    private final ReceiveAddressWriteService receiveAddressWriteService;

    @Autowired
    public ReceiveAddresses(ReceiveAddressReadService receiveAddressReadService,
                            ReceiveAddressWriteService receiveAddressWriteService) {
        this.receiveAddressReadService = receiveAddressReadService;
        this.receiveAddressWriteService = receiveAddressWriteService;
    }

    /**
     * 添加收货地址
     *
     * @param receiveAddress 收货地址信息
     * @return 收货地址ID
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createReceiveAddress(@RequestBody ReceiveAddress receiveAddress){
        ParanaUser user = UserUtil.getCurrentUser();
        // 设置用户ID, 状态为可用(1),
        receiveAddress.setUserId(user.getId());
        receiveAddress.setStatus(1);
        receiveAddress.setIsDefault(false);
        Response<Long> resp = receiveAddressWriteService.createReceiveAddress(receiveAddress);
        if (!resp.isSuccess()) {
            log.error("failed to create receive address by userId = {}, error : {}", user.getId(), resp.getError());
            throw new JsonResponseException(500, resp.getError());
        }
        return resp.getResult();
    }

    /**
     * 获取当前用户的收货地址
     *
     * @return 收货地址信息
     */
    @RequestMapping(value = "/getReceiveAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReceiveAddress> getReceiveAddress() {
        ParanaUser user = UserUtil.getCurrentUser();
        Response<List<ReceiveAddress>> rResult = receiveAddressReadService.findAddressByUserId(user.getId());
        if (!rResult.isSuccess()) {
            log.error("failed to get receive address by userId = {}, error : {}", user.getId(), rResult.getError());
            throw new JsonResponseException(rResult.getError());
        }
        return rResult.getResult();
    }

    /**
     * 更新收货地址信息
     *
     * @param receiveAddress 收货地址信息
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateReceiveAddress(@RequestBody ReceiveAddress receiveAddress){
        ParanaUser user = UserUtil.getCurrentUser();
        receiveAddress.setUserId(user.getId());
        Response<Boolean> resp = receiveAddressWriteService.updateReceiveAddress(receiveAddress);
        if (!resp.isSuccess()) {
            log.error("failed to update receive address by userId = {}, addressId = {}, error : {}",
                    user.getId(), receiveAddress.getId(), resp.getError());
            throw new JsonResponseException(resp.getError());
        }
        return Boolean.TRUE;
    }

    /**
     * 删除当前用户的收货地址
     *
     * @param addressId 收货地址ID
     * @return
     */
    @RequestMapping(value = "/delete/{addressId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean deleteReceiveAddress(@PathVariable Long addressId){
        ParanaUser user = UserUtil.getCurrentUser();
        Long userId = user.getId();
        Response<Boolean> resp = receiveAddressWriteService.deleteAddressByAddressIdAndUserId(addressId, userId);
        if (!resp.isSuccess()) {
            log.error("failed to delete receive address by receiveId = {}, userId = {}, error : {}",
                    addressId, userId, resp.getError());
            throw new JsonResponseException(resp.getError());
        }
        return Boolean.TRUE;
    }

    /**
     * 设置默认收货地址
     *
     * @param addressId 收货地址ID
     * @return
     */
    @RequestMapping(value = "/setDefault/{addressId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean setAddressDefault(@PathVariable Long addressId){
        Long userId = UserUtil.getCurrentUser().getId();
        Response<Boolean> resp = receiveAddressWriteService.makeDefault(addressId, userId);
        if (!resp.isSuccess()) {
            log.error("failed to set address default, addressId = {}, userId = {}, error : {}",
                    addressId, userId, resp.getError());
            throw new JsonResponseException(resp.getError());
        }
        return Boolean.TRUE;
    }

}
