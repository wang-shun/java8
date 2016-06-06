package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 用户服务状态dto
 * author: 陈增辉
 * Date: 16/5/18
 */
@Data
public class DoctorServiceReviewDto implements Serializable {
    private static final long serialVersionUID = -7409472199068574853L;

    private Long userId;

    private ServiceReviewOpenDto pigDoctor;  //猪场软件
    private ServiceReviewOpenDto pigmall;    //新融电商
    private ServiceReviewOpenDto neverest;   //大数据
    private ServiceReviewOpenDto pigTrade;   //生猪交易

}
