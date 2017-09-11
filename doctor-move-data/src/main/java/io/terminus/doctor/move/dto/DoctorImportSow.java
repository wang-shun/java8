package io.terminus.doctor.move.dto;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * modifier:xjn
 * Date: 2016/10/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorImportSow implements Serializable {
    private static final long serialVersionUID = 3303665058775831200L;

    private Integer lineNumber;      //excel中行号
    private String barnName;         //猪舍
    private String sowCode;          //母猪耳号
    private Integer status;          //当前状态(遗弃)
    private String currentStatus;    //当前状态
    private Integer parity;          //胎次
    private Date mateDate;           //配种日期
    private String boarCode;         //公猪耳号
    private String mateStaffName;    //配种员
    private Date prePregDate;        //预产日期
    private Date pregDate;           //实产日期
    private String farrowBarnName;   //分娩猪舍(历史胎次默认产房中第一个产房)
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
    private Date inFarmDate;         //进场日期

    private Date pregCheckDate;      //妊娠检查日期(默认配种日期加三周,大于当前时间取当前时间)
    private String pregBarn;         //进场、配种、妊娠检查、去分娩几个事件的发生猪舍(默认导入猪舍的第一个妊娠舍)
    private String weanToBarn;       //断奶转入猪舍
    private Integer parityStage;     //胎次阶段
    private String pregCheckResult;   //妊娠检查结果

    public enum ParityStage {
        FIRST(1, "第一条"),
        FIRST_PRE(2, "第一条并且是当前胎次前一个"),
        FIRST_CURRENT(3, "第一条并且是当前胎次"),
        FIRST_CURRENT_LAST(4, "第一条并且是当前胎次最后一个"),
        MIDDLE(5, "中间胎次"),
        MIDDLE_PRE(6, "中间胎次并且是当前胎次前一个"),
        CURRENT(7, "当前胎次"),
        CURRENT_LAST(8, "当前胎次并且是最后一条");

        ParityStage(Integer value, String name) {
            this.value = value;
            this.desc = name;
        }
        @Getter
        private Integer value;
        @Getter
        private String desc;

        public static List<Integer> firsts = Lists.newArrayList(FIRST.getValue(), FIRST_PRE.getValue(),
                FIRST_CURRENT.getValue(), FIRST_CURRENT_LAST.getValue());
        public static List<Integer> currentLasts = Lists.newArrayList(FIRST_CURRENT_LAST.getValue(), CURRENT_LAST.getValue());
        public static List<Integer> pres = Lists.newArrayList(FIRST_PRE.getValue(), MIDDLE_PRE.getValue());

    }
}
