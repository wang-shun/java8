package io.terminus.doctor.web.core.service.impl;

import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.web.core.dto.ServiceBetaStatusToken;
import io.terminus.doctor.web.core.service.ServiceBetaStatusHandler;
import io.terminus.parana.config.ConfigCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 陈增辉 on 16/6/17.
 * 服务的状态除了开通和未开通之外,还有一个特殊的状态:内测中,处于内测中的服务是不可用的,连申请都不可以
 * 该Service就是用于查询服务是否处于内测中
 */
@Service
public class ServiceBetaStatusHandlerImpl implements ServiceBetaStatusHandler {
    private final ConfigCenter configCenter;
    private final DoctorServiceStatusWriteService doctorServiceStatusWriteService;

    @Autowired
    public ServiceBetaStatusHandlerImpl(ConfigCenter configCenter,
                                        DoctorServiceStatusWriteService doctorServiceStatusWriteService){
        this.configCenter = configCenter;
        this.doctorServiceStatusWriteService = doctorServiceStatusWriteService;
    }

    @Override
    public ServiceBetaStatusToken getServiceBetaStatusToken() {
        ServiceBetaStatusToken dto = new ServiceBetaStatusToken();
        dto.setPigdoctor(configCenter.get("user.service.pigdoctor.beta").or(ServiceBetaStatusToken.Status.OPEN.value()));
        dto.setPigmall(configCenter.get("user.service.pigmall.beta").or(ServiceBetaStatusToken.Status.OPEN.value()));
        dto.setNeverest(configCenter.get("user.service.neverest.beta").or(ServiceBetaStatusToken.Status.OPEN.value()));
        dto.setPigtrade(configCenter.get("user.service.pigtrade.beta").or(ServiceBetaStatusToken.Status.OPEN.value()));
        dto.setBetaDesc(configCenter.get("user.service.beta.desc").or(ServiceBetaStatusToken.Status.BETA.toString()));
        return dto;
    }

    @Override
    public void initDefaultServiceStatus(Long userId){
        ServiceBetaStatusToken dto = this.getServiceBetaStatusToken();
        DoctorServiceStatus status = new DoctorServiceStatus();
        status.setUserId(userId);

        //猪场软件初始状态
        if(Objects.equals(dto.getPigdoctor(), ServiceBetaStatusToken.Status.BETA.value())){
            status.setPigdoctorStatus(DoctorServiceStatus.Status.BETA.value());
            status.setPigdoctorReason(dto.getBetaDesc());
        }else{
            status.setPigdoctorStatus(DoctorServiceStatus.Status.CLOSED.value());
        }
        status.setPigdoctorReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        //电商初始状态
        if(Objects.equals(dto.getPigmall(), ServiceBetaStatusToken.Status.BETA.value())){
            status.setPigmallStatus(DoctorServiceStatus.Status.BETA.value());
            status.setPigmallReason(dto.getBetaDesc());
        }else{
            status.setPigmallStatus(DoctorServiceStatus.Status.CLOSED.value());
        }
        status.setPigmallReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        //大数据初始状态
        if(Objects.equals(dto.getNeverest(), ServiceBetaStatusToken.Status.BETA.value())){
            status.setNeverestStatus(DoctorServiceStatus.Status.BETA.value());
            status.setNeverestReason(dto.getBetaDesc());
        }else{
            status.setNeverestStatus(DoctorServiceStatus.Status.CLOSED.value());
        }
        status.setNeverestReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        //猪场软件初始状态
        if(Objects.equals(dto.getPigtrade(), ServiceBetaStatusToken.Status.BETA.value())){
            status.setPigtradeStatus(DoctorServiceStatus.Status.BETA.value());
            status.setPigtradeReason(dto.getBetaDesc());
        }else{
            status.setPigtradeStatus(DoctorServiceStatus.Status.CLOSED.value());
        }
        status.setPigtradeReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        doctorServiceStatusWriteService.createServiceStatus(status);
    }
}
