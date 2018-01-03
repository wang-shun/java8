package io.terminus.doctor.basic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/12/27.
 */
@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class DoctorReportFieldTypeDto implements Serializable {


    @NotNull(message = "report.customize.type.id.null")
    private Long id;

    private String name;

    @Valid
    private List<DoctorReportFieldDto> fields;


    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorReportFieldDto implements Serializable {

        @NotNull(message = "report.customize.type.id.null")
        private Long id;

        private String name;

        private Boolean hidden;
    }


}
