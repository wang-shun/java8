package io.terminus.doctor.move.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Date;

/**
 * Created by xjn on 17/8/29.
 * 公猪信息页
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorImportBoar {

    private Integer lineNumber;     //所在Excel耳号
    private String barnName;        //猪舍
    private String boarCode;        //公猪号
    private Date inFarmIn;          //进场如期
    private Date birthday;          //出生日期
    private String fatherCode;      //父号
    private String motherCOde;      //母号
    private String breedName;       //品种
    private String source;          //来源
    private String boarType;        //公猪类型
    private Double origin;          //原值
}
