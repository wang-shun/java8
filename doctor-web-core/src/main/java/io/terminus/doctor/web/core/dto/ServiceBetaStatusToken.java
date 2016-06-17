package io.terminus.doctor.web.core.dto;

import io.terminus.doctor.user.model.DoctorServiceReview;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 陈增辉 on 16/6/17.
 * 服务的状态除了开通和未开通之外,还有一个特殊的状态:内测中,处于内测中的服务是不可用的,连申请都不可以
 * 该dto就是用于查询服务是否处于内测中
 */
@Data
public class ServiceBetaStatusToken implements Serializable{
    private static final long serialVersionUID = -7243068897726350914L;

    /**
     * 猪场软件服务是否处于内测中,1-内测中,0-已开放使用
     */
    private String pigdoctor;
    /**
     * 电商服务是否处于内测中,1-内测中,0-已开放使用
     */
    private String pigmall;
    /**
     * 大数据服务是否处于内测中,1-内测中,0-已开放使用
     */
    private String neverest;
    /**
     * 生猪交易服务是否处于内测中,1-内测中,0-已开放使用
     */
    private String pigtrade;

    /**
     * 对于内测这种特殊状态的描述,其值来自configcenter
     */
    private String betaDesc;

    public enum Status{
        BETA("1", "内测中"),
        OPEN("0", "已开放使用");

        private String value;
        private String desc;

        Status(String value, String desc){
            this.value = value;
            this.desc = desc;
        }

        public String value(){
            return this.value;
        }
        public String toString(){
            return this.desc;
        }
    }

    public boolean inBeta(DoctorServiceReview.Type serviceType){
        String betaStatus;
        if(Objects.equals(DoctorServiceReview.Type.PIG_DOCTOR, serviceType)){
            betaStatus = this.pigdoctor;
        }else if(Objects.equals(DoctorServiceReview.Type.PIGMALL, serviceType)){
            betaStatus = this.pigmall;
        }else if(Objects.equals(DoctorServiceReview.Type.NEVEREST, serviceType)){
            betaStatus = this.neverest;
        }else if(Objects.equals(DoctorServiceReview.Type.PIG_TRADE, serviceType)){
            betaStatus = this.pigtrade;
        }else{
            throw new IllegalArgumentException("doctor.service.review.type.error");
        }
        return Objects.equals(betaStatus, Status.BETA.value());
    }
}
