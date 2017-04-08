package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.enums.MatingType;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by terminus on 2017/4/8.
 */
@Data
public class DoctorNpdExportDto implements Serializable{

    private static final long serialVersionUID = 966209148511871287L;

    private String pigCode;
    private String barnName;

    /**
     * 进场到配种
     */
    private Integer jpndp = 0;
    /**
     * 断奶到配种的非生产天数
     */
    private Integer dpnpd = 0;

    /**
     * 配种到返情非生产天数
     */
    private Integer pfnpd = 0;

    /**
     * 配种到流产非生产天数
     */
    private Integer plnpd = 0;

    /**
     * 配种到死亡非生产天数
     */
    private Integer psnpd = 0;

    /**
     * 配种到淘汰非生产天数
     */
    private Integer ptnpd = 0;
    /**
     * 非生产天数 前面的总和
     */
    private Integer npd = 0;

}
