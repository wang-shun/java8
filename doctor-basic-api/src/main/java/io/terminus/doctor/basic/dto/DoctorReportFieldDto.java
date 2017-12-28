package io.terminus.doctor.basic.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.context.annotation.Bean;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/12/27.
 */
@Data
public class DoctorReportFieldDto implements Serializable{


    @NotNull(message = "report.customize.type.id.null")
    private Long id;

    private String name;

    private List<DoctorReportFieldDto> fields;

}
