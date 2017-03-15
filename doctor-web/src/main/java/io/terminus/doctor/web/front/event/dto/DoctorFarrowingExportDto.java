package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorFarrowingExportDto implements Serializable{

    private static final long serialVersionUID = 1983370736664800946L;

    private String pigCode;
    private Integer parity;
    private String barnName;
    private Date farrowingDate; // 分娩日期
    private String nestCode; // 窝号
    private String bedCode; // 床号
    private Integer farrowingType;  // 分娩类型
    private double birthNestAvg;    //出生窝重
    private Integer farrowingLiveCount; // 分娩猪的数量 = 活公猪 + 活母猪 = 健崽数量 + 弱仔数量 (拼窝后， 可能不相等， 差值 拼窝数量)
    private Integer liveSowCount;   //  活母猪数量
    private Integer liveBoarCount;  // 活公猪数量
    private Integer healthCount;    //健仔数量(断奶事件校验对应的母猪状态信息)
    private Integer weakCount;  //  弱崽数量
    private Integer mnyCount;   // 木乃伊数量
    private Integer jxCount;    // 畸形数量
    private Integer deadCount;  // 死亡数量
    private Integer blackCount; //  黑太数量
    private String toBarnName;  //猪群相关 待定！！
    private String farrowStaff1;  //接生员1
    private String farrowStaff2;  //接生员2
    private String farrowRemark;    //标志
    private String updatorName;
}
