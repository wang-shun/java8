package io.terminus.doctor.event.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/4/12.
 * 当前存栏状况
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorLiveStockDto implements Serializable{
    private static final long serialVersionUID = -7206508556530619891L;

    /**
     * 猪存栏
     */
    private DoctorPigCountDto pigCountDto;

    /**
     * 猪群存栏
     */
    private DoctorGroupCountDto groupCountDto;
}
