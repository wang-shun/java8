package io.terminus.doctor.basic.dto;

import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStockHandlerDetail;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/8/18.
 */
@Data
public class DoctorWarehouseStockHandleDto implements Serializable{


    private static final long serialVersionUID = -4992570075179699960L;
    private Date handleDate;

    private BigDecimal number;

    private DoctorWarehouseStock stock;

    private DoctorWarehouseStockHandlerDetail handleDetail;
}
