package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MaterialCountAmount implements Serializable{
    private static final long serialVersionUID = 8845899857608972994L;

    private Long count;
    private Double amount;
}
