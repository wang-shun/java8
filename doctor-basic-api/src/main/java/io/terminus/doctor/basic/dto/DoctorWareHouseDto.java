package io.terminus.doctor.basic.dto;

import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.DoctorWareHouseTrack;
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

    private Long farmId;

    private String farmName;

    private Long warehouseId;

    private String warehouseName;

    private String manager;

    private Double remainder;

    private Date recentlyConsume;   //最近领用

    private Integer restConsumeDate;    // 剩余使用日期

    private Integer type; //仓库类型

    private Double stockCount;   //库存数量(只有饲料、原料类型仓库用到,单位统一是kg,其他仓库单位不统一)

    private Double stockAmount;  //库存金额

    private Double monthInCount; //本月入库数量(同库存,只有饲料、原料类型仓库用到)

    private Double monthInAmount; //本月入库金额

    private Double monthOutCount; //本月出库数量(同库存,只有饲料、原料类型仓库用到)

    private Double monthOutAmount; //本月出库金额

    private Double monthTransferInCount; //本月调入数量(同库存,只有饲料、原料类型仓库用到)

    private Double monthTransferInAmount; //本月调入金额

    private Double monthTransferOutCount; //本月调出数量(同库存,只有饲料、原料类型仓库用到)

    private Double monthTransferOutAmount; //本月调出金额

    public static DoctorWareHouseDto buildWareHouseDto(DoctorWareHouse doctorWareHouse, DoctorWareHouseTrack doctorWareHouseTrack){
        if(isNull(doctorWareHouse)){
            return null;
        }

        DoctorWareHouseDtoBuilder builder = DoctorWareHouseDto.builder()
                .farmId(doctorWareHouse.getFarmId())
                .farmName(doctorWareHouse.getFarmName())
                .warehouseId(doctorWareHouse.getId())
                .warehouseName(doctorWareHouse.getWareHouseName())
                .manager(doctorWareHouse.getManagerName())
                .type(doctorWareHouse.getType());

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
            builder.restConsumeDate(Double.valueOf(extraMap.get(REST_CONSUME_DATE).toString()).intValue());
        }
        return builder.build();
    }

    private String transFormUnit(Integer type){
        String unit = "kg";
        switch (type){
            case 3:
                unit = "g";
                break;
            case 4:
                unit = "g";
                break;
        }
        return unit;
    }
}
