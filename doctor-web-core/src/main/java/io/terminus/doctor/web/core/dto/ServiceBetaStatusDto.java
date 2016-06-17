package io.terminus.doctor.web.core.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 陈增辉 on 16/6/17.
 * 服务的状态除了开通和未开通之外,还有一个特殊的状态:内测中,处于内测中的服务是不可用的,连申请都不可以
 * 该dto就是用于查询服务是否处于内测中
 */
@Data
public class ServiceBetaStatusDto implements Serializable{
    private static final long serialVersionUID = -7243068897726350914L;

    private String pigdoctor;

    private String pigmall;

    private String neverest;

    private String pigtrade;

    /**
     * 对于内测这种特殊状态的描述
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
}
