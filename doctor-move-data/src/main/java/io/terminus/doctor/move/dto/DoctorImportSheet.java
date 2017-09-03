package io.terminus.doctor.move.dto;

import lombok.Data;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */
@Data
public class DoctorImportSheet implements Serializable {
    private static final long serialVersionUID = 9011601457319364135L;

    private Sheet farm;       //猪场信息
    private Sheet staff;      //员工
    private Sheet barn;       //猪舍
    private Sheet breed;      //品种
    private Sheet sow;        //母猪
    private Sheet boar;       //公猪
    private Sheet group;      //猪群
    private Sheet warehouse;  //仓库
    private Sheet medicine;   //药品
    private Sheet vacc;       //疫苗
    private Sheet material;   //原料
    private Sheet feed;       //饲料
    private Sheet consume;    //易耗品
}
