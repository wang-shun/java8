package io.terminus.doctor.basic.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/8.
 */
@Data
public class WarehouseStockDto implements Serializable{


    private static final long serialVersionUID = 3678280551400454014L;
    
    @NotNull(message = "warehouse.id.null",groups = DefaultWarehouseValid.class)
    private Long warehouseID;

    @NotNull(message = "farm.id.null",groups = DefaultWarehouseValid.class)
    private Long farmID;

    private Integer type;

    @NotNull(message = "handler.date.null",groups = DefaultWarehouseValid.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date handlerDate;

    @NotNull(message = "operator.id.null",groups = DefaultWarehouseValid.class)
    private Long operatorID;

    @Valid
    @NotEmpty(message = "handler.detail.empty",groups = DefaultWarehouseValid.class)
    private List<WarehouseStockDetailDto> details;



    public  interface  DefaultWarehouseValid{

    }

    public interface InWarehouseValid extends DefaultWarehouseValid{

    }

    public interface OutWarehouseValid extends DefaultWarehouseValid{

    }

    public interface InventoryWarehouseValid extends DefaultWarehouseValid{

    }

    public interface TransferWarehouseValid extends DefaultWarehouseValid{

    }

}
