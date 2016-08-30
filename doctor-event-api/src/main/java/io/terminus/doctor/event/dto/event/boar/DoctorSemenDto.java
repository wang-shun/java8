package io.terminus.doctor.event.dto.event.boar;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.AbstractPigEventInputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSemenDto extends AbstractPigEventInputDto implements Serializable{

    private static final long serialVersionUID = 3227572350710428642L;

    private Date semenDate;     //采精日期

    private Double semenWeight; //采精重量

    private Double dilutionRatio;   // 稀释倍数

    private Double dilutionWeight;  //稀释后重量

    private Double semenDensity;    //精液密度

    private Double semenActive; //精液活力

    private Double semenPh; //精液PH

    private Double semenTotal;  //精液总量

    private Double semenJxRatio;    //精液畸形率

    private String semenRemark; //精液备注（非必填）

    @Override
    public Map<String, String> descMap() {
        Map<String, String> map = new HashMap<>();
        if(semenDate != null){
            map.put("日期", DateUtil.toDateString(semenDate));
        }
        if(semenWeight != null){
            map.put("重量", semenWeight.toString());
        }
        if(dilutionRatio != null){
            map.put("稀释倍数", dilutionRatio.toString());
        }
        if(dilutionWeight != null){
            map.put("稀释后重量", dilutionWeight.toString());
        }
        if(semenDensity != null){
            map.put("密度", semenDensity.toString());
        }
        if(semenActive != null){
            map.put("精液活力", semenActive.toString());
        }
        if(semenPh != null){
            map.put("PH", semenPh.toString());
        }
        if(semenTotal != null){
            map.put("精液总量", semenTotal.toString());
        }
        if(semenJxRatio != null){
            map.put("畸形率", semenJxRatio.toString());
        }
        return map;
    }

    @Override
    public Date eventAt() {
        return this.semenDate;
    }
}
