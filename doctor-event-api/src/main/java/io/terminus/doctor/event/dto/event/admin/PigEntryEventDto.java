package io.terminus.doctor.event.dto.event.admin;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 猪进场事件
 * Created by sunbo@terminus.io on 2017/9/8.
 */
public class PigEntryEventDto extends PigEventDto {


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "birthday.not.null")
    private Date birthday;

    /**
     * 进场日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date inFarmDate;

    private String pigCode;

    @NotNull(message = "source.not.null")
    private Integer source;

    /**
     * 品种
     */
    private Long breed;

    /**
     * 品系
     */
    private Long breedType;

    /**
     * 胎次
     */
    private Integer parity;

    /**
     * 父猪号
     */
    private String fatherCode;

    /**
     * 母猪号
     */
    private String motherCode;


    /**
     * 耳缺号
     */
    private String earCode;

    /**
     * 左乳头数
     */
    private Integer left;

    /**
     * 右乳头数
     */
    private Integer right;
}
