package io.terminus.doctor.web.front.warehouseV2.vo;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 物料变动报表
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseMaterialHandleVo {


    @JsonView(MaterialHandleDefaultView.class)
    private String materialName;

    @JsonView(MaterialHandleReportView.class)
    private Integer type;

    @JsonView(MaterialHandleDefaultView.class)
    private Date handleDate;

    @JsonView(MaterialHandleDefaultView.class)
    private BigDecimal quantity;

    @JsonView(MaterialHandleDefaultView.class)
    private Long unitPrice;

    @JsonView(MaterialHandleReportView.class)
    private String pigBarnName;

    @JsonView(MaterialHandleReportView.class)
    private String pigGroupName;

    @JsonView(MaterialHandleReportView.class)
    private String warehouseName;

    @JsonView(MaterialHandleReportView.class)
    private String transferInWarehouseName;

    @JsonView(MaterialHandleReportView.class)
    private String vendorName;

    @JsonView(MaterialHandleReportView.class)
    private String unit;

    @JsonView(MaterialHandleReportView.class)
    private String code;

    @JsonView(MaterialHandleReportView.class)
    private String specification;


    /**
     * 默认视图
     */
    public interface MaterialHandleDefaultView {

    }

    /**
     * 物料变动明细报表视图
     */
    public interface MaterialHandleReportView extends MaterialHandleDefaultView {
    }

    /**
     * 仓库事件视图
     */
    public interface MaterialHandleEventView extends MaterialHandleDefaultView {
    }


}
