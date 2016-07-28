package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_GainCardList implements Serializable{
    private static final long serialVersionUID = 5256246992598006428L;
    private String outId;
    private Date openAt;
    private Date closeAt;
    private Integer status;      //转换后的状态 1 -1
    private String farmOutId;
    private String groupCode;
    private Integer sex;         //转换后的性别 0 1 2
    private String breedName;
    private String geneticName;
    private String remark;
    private String barnOutId;
    private String staffName;
}
