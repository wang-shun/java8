package io.terminus.doctor.basic.dto;

import lombok.Data;

/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
@Data
public class Pageable {

    private int pageNo = 1;

    private int pageSize = 10;
}
