package io.terminus.doctor.basic.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/12/27.
 */
@Data
public class DoctorReportFieldTypeDto implements Serializable {


    private static final long serialVersionUID = 7006221694137190527L;
    @NotNull(message = "report.customize.type.id.null")
    private Long id;

    private String name;

    @JsonIgnore
    private String reportField;

    @Valid
    private List<DoctorReportFieldDto> fields;


    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorReportFieldDto implements Serializable {

        private static final long serialVersionUID = 4876768912712007938L;
        @NotNull(message = "report.customize.type.id.null")
        private Long id;

        private String name;

        @JsonIgnore
        private String reportField;

        @JsonIgnore
        private String dataFormatter;

        private Boolean hidden;

        private String value;
    }


}
