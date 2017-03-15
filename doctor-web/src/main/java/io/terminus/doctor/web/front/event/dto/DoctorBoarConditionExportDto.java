package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorBoarConditionExportDto implements Serializable{

    private static final long serialVersionUID = 7602179257279806923L;

    private String pigCode;
    private String barnName;
    private Date checkAt;
    /**
     * 活力得分
     */
    private Integer scoreHuoli;

    /**
     * 密度得分
     */
    private Integer scoreMidu;

    /**
     * 形态得分
     */
    private Integer scoreXingtai;

    /**
     * 数量得分
     */
    private Integer scoreShuliang;
    /**
     * 重量(必填)
     */
    private Double weight;
    /**
     * 备注
     */
    private String remark;
    private String updatorName;
    private String operatorName;
}
