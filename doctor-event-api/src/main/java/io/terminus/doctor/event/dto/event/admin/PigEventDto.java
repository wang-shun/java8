package io.terminus.doctor.event.dto.event.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/9/8.
 */
@Data
public class PigEventDto {


    private Long farmId;

    @NotNull(message = "god.pig.event.id.null")
    private Long eventId;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "birthday.not.null", groups = EntryEventValid.class)
    private Date birthday;

    /**
     * 进场日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date inFarmDate;

    private String pigCode;

    @NotNull(message = "source.not.null", groups = EntryEventValid.class)
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


    private String remark;


    public interface EntryEventValid {
    }

}
