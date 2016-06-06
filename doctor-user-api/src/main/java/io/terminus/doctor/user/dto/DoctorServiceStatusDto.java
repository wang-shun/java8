package io.terminus.doctor.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 用户服务状态dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Data
public class DoctorServiceStatusDto implements Serializable {
    private static final long serialVersionUID = 4115503567351751985L;

    private Long userId;

    /**
     * 以下4个是服务的状态, 0:未提交申请;  2:已提交申请,正在审核中;  1:已开通;  -1:审核不通过;  -2:冻结申请资格;
     */
    private Integer pigDoctor;  //猪场软件
    private Integer pigmall;    //新融电商
    private Integer neverest;   //大数据
    private Integer pigTrade;   //生猪交易

    //下面4个是审核不通过或申请资格被冻结的原因
    private String pigDoctorReason;
    private String pigmallReason;
    private String neverestReason;
    private String pigTradeReason;
}
