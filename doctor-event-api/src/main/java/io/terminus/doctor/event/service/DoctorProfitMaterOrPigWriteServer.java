package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;

import java.util.List;
import java.util.Date;

/**
 * 物料和猪类型的报表数据写入
 * Created by terminus on 2017/4/12.
 */
public interface DoctorProfitMaterOrPigWriteServer {
    /**
     * 更新数据物料和猪类型的报表数据
     * @param doctorProfitMaterialOrPig
     * @return
     */
    public Response<Boolean> updateDoctorProfitMaterialOrPig(List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPig);
    /**
     * 插入数据物料和猪类型的报表数据
     * @param doctorProfitMaterialOrPig
     */
    public Response<Boolean> insterDoctorProfitMaterialOrPig(List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPig);

    /**
     * 删除数据，当已存在报表数据时
     *
     */
    public Response<Boolean> deleteDoctorProfitMaterialOrPig(Date sumTime);

}
