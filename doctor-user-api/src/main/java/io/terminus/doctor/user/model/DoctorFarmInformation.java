package io.terminus.doctor.user.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author:
 * @Date: 2018/8/28 14:42
 * @Description:
 */
public class DoctorFarmInformation implements Serializable {

    /**
     * 公司id
     */
//    private int orgId;

    /**
     * 公司名称
     */
    private String orgName;

    /**
     * 记录时间
     */
    private Date record_at;

    /**
     * 猪场id
     */
    private int farmId;

    /**
     * 猪场名称
     */
    private String farmName;

    /**
     * 猪场地址
     */
    private String address;

    /**
     * 法人
     */
    private String legalPerson;

    /**
     * 法人电话
     */
    private String personPhone;

    /**
     * 负责人
     */
    private String head;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 建厂日期
     */
    private Date factoryDate;

    /**
     * 建筑面积
     */
    private float construction;

    /**
     * 产能
     */
    private  Integer capacity;

    /**
     * 母猪存栏数量
     */
    private  Integer sowAmount;

    /**
     * 配怀母猪存栏数量
     */
//    private  Integer pregnantSowAmount;

    /**
     * 产房母猪存栏数量
     */
//    private  Integer roomSowAmount;

    /**
     * 公猪存栏数量
     */
    private  Integer boarAmount;

    /**
     * 保育猪存栏数量
     */
    private  Integer conservationAmount;

    /**
     * 育肥猪存栏数量
     */
    private  Integer fatteningAmount;

    /**
     * 后备猪存栏数量
     */
//    private  Integer backupAmount;

    /**
     * 仔猪存栏数量
     */
    private  Integer pigletsAmount;


}
