package io.terminus.doctor.move.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/21
 */
@Data
public class DoctorImportSow implements Serializable {
    private static final long serialVersionUID = 3303665058775831200L;

    private String barnName;         //猪舍
    private String sowCode;          //母猪耳号
    private Integer status;          //当前状态
    private Integer parity;          //胎次
    private Date mateDate;           //配种日期
    private String boarCode;         //公猪耳号
    private String mateStaffName;    //配种员
    private Date prePregDate;        //预产日期
    private Date pregDate;           //实产日期
    private String farrowBarnName;   //分娩猪舍
    private String bed;              //床号
    private Date weanDate;           //断奶日期
    private Integer liveCount;       //活仔数
    private Integer jixingCount;     //畸形
    private Integer weakCount;       //弱仔数
    private Integer deadCount;       //死仔
    private Integer mummyCount;      //木乃伊
    private Integer blackCount;      //黑胎
    private Double nestWeight;       //窝重
    private String staff1;           //接生员1
    private String staff2;           //接生员2
    private String sowEarCode;       //母猪耳号
    private Date birthDate;          //出生日期
    private String remark;           //备注
    private String breed;            //品种
    private Double weanWeight;       //断奶重
    private Integer weanCount;       //断奶数
    private String fatherCode;       //父号
    private String motherCode;       //母号
    private Date inFarmDate;          //进场日期
}
