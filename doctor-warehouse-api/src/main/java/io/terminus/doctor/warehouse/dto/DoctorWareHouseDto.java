package io.terminus.doctor.warehouse.dto;

import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 仓库列表信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWareHouseDto implements Serializable{

    private static final long serialVersionUID = 317867907937075936L;

    private String warehouseName;

    private String manager;

    private Long remainder;

    private Date recentlyConsume;   //最精领用

    public static DoctorWareHouseDto buildWareHouseDto(DoctorWareHouse doctorWareHouse, DoctorWareHouseTrack doctorWareHouseTrack){
        if(isNull(doctorWareHouse)){
            return null;
        }

        DoctorWareHouseDtoBuilder builder = DoctorWareHouseDto.builder().warehouseName(doctorWareHouse.getWareHouseName())
                .manager(doctorWareHouse.getManagerName());

        if(!isNull(doctorWareHouseTrack)){
            builder.remainder(doctorWareHouseTrack.getLotNumber()).recentlyConsume(doctorWareHouseTrack.getUpdatedAt());
        }
        return builder.build();
    }

}
