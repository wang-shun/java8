package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CompanyReportDto implements Serializable,Comparable<CompanyReportDto> {

    //结算月
    private Integer settlementMonth;

    //结算日期
    private Date settlementDate;

    //猪场Id
    private Long farmId;

    //猪场名称
    private String farmName;

    //结余
    private BigDecimal balanceAmount;

    //入库
    private  BigDecimal inAmount;

    //出库
    private BigDecimal outAmount;

    //最后结算时间
    private Date lastSettlementDate;

    //总入库金额
    private BigDecimal allInAmount;

    //总出库金额
    private BigDecimal allOutAmount;

    //总结余金额
    private BigDecimal allBalanceAmount;

    //是否结算
    private Boolean settled;


    @Override
    public int compareTo(CompanyReportDto o) {
        long i = this.getSettlementDate().getTime() - o.getSettlementDate().getTime();
        if(i==0){
            i = this.getFarmId() - o.getFarmId();
        }
        return (int)i;
    }

}
