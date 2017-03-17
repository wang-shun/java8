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

    private Long pigId;

    /**
     * 来源事件id
     */
    private Long fromEventId;

    /**
     * 导致变化事件id
     */
    private Long toEventId;

    /**
     * 事件发生后镜像
     * @see io.terminus.doctor.event.dto.DoctorPigSnapShotInfo
     */
    private String toPigInfo;

    private Date createdAt;

    private Date updatedAt;
}