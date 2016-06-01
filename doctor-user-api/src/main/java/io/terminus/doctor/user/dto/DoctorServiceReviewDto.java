package io.terminus.doctor.user.dto;

import io.terminus.doctor.user.model.DoctorServiceReview;
import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 用户服务审批列转行
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Data
public class DoctorServiceReviewDto implements Serializable {
    private static final long serialVersionUID = 2960622326826290246L;

    private Long userId;

    private DoctorServiceReview pigDoctor;  //猪场软件

    private DoctorServiceReview pigmall;    //新融电商

    private DoctorServiceReview neverest;   //大数据

    private DoctorServiceReview pigTrade;   //生猪交易

    //下面4个是服务被冻结或审核不通过的原因
    private String pigDoctorReason;
    private String pigmallReason;
    private String neverestReason;
    private String pigTradeReason;
}
