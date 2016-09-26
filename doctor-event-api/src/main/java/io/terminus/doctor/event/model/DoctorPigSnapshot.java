package io.terminus.doctor.event.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.common.utils.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPigSnapshot implements Serializable{

    private static final long serialVersionUID = -7819883927315891506L;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private Long id;

    private Long orgId;

    private Long farmId;

    private Long pigId;

    private Long eventId;

    /**
     * 当前事件发生前的info
     * @see io.terminus.doctor.event.dto.DoctorPigSnapShotInfo
     */
    private String pigInfo;

    private Date createdAt;

    private Date updatedAt;
}