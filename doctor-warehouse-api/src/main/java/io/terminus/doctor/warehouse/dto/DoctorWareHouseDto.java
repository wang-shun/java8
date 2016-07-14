package io.terminus.doctor.warehouse.dto;

import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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

    private static final String RECENT_CONSUME_DATE = "recentConsumeDate";

    private static final String REST_CONSUME_DATE = "restConsumeDate";

    private Long warehouseId;

    private String warehouseName;

    private String manager;

    private Long remainder;

    private Date recentlyConsume;   //最近领用

    private Integer restConsumeDate;    // 剩余使用日期

    public static DoctorWareHouseDto buildWareHouseDto(DoctorWareHouse doctorWareHouse, DoctorWareHouseTrack doctorWareHouseTrack){
        if(isNull(doctorWareHouse)){
            return null;
        }

        DoctorWareHouseDtoBuilder builder = DoctorWareHouseDto.builder()
                .warehouseId(doctorWareHouse.getId())
                .warehouseName(doctorWareHouse.getWareHouseName())
                .manager(doctorWareHouse.getManagerName());

        Map<String,Object> extraMap = doctorWareHouseTrack.getExtraMap();

        if(!isNull(doctorWareHouseTrack)){
            builder.remainder(doctorWareHouseTrack.getLotNumber());
        }

        if(isNull(extraMap)) {
            return builder.build();
        }

        if(extraMap.containsKey(RECENT_CONSUME_DATE)){
            builder.recentlyConsume(new Date((Long) extraMap.get(RECENT_CONSUME_DATE)));
        }

        if(extraMap.containsKey(REST_CONSUME_DATE)){
            builder.restConsumeDate(Integer.valueOf(extraMap.get(REST_CONSUME_DATE).toString()));
        }
        return builder.build();
    }
}
