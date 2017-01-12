package io.terminus.doctor.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xjn on 17/1/12.
 * 用于封装suggespig信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSuggestPig implements Serializable{
    private static final long serialVersionUID = 1804740563738111152L;

    private Long id;

    private String pigCode;
}
