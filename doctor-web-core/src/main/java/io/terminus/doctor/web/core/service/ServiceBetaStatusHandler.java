package io.terminus.doctor.web.core.service;

import io.terminus.doctor.web.core.dto.ServiceBetaStatusToken;

/**
 * 陈增辉 on 16/6/17.
 * 服务的状态除了开通和未开通之外,还有一个特殊的状态:内测中,处于内测中的服务是不可用的,连申请都不可以
 * 该Service就是用于查询服务是否处于内测中
 */
public interface ServiceBetaStatusHandler {

    ServiceBetaStatusToken getServiceBetaStatusToken();

    void initDefaultServiceStatus(Long userId);
}
