package io.terminus.doctor.warehouse.handler.in;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialPriceInWareHouseDao;
import io.terminus.doctor.warehouse.dao.MaterialFactoryDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialPriceInWareHouse;
import io.terminus.doctor.warehouse.model.MaterialFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorProviderEventHandler implements IHandler{

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;
    private final DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao;
    private final MaterialFactoryDao materialFactoryDao;

    @Autowired
    public DoctorProviderEventHandler(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao,
                                      DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao,
                                      MaterialFactoryDao materialFactoryDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
        this.doctorMaterialPriceInWareHouseDao = doctorMaterialPriceInWareHouseDao;
        this.materialFactoryDao = materialFactoryDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getActionType());
        return eventType != null && eventType.isIn();
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getActionType());
        // 常规添加时, 必须有单价, 单价由用户自行填写; 调入时单价由系统计算
        if((dto.getUnitPrice() == null || dto.getUnitPrice() <= 0)
                && (eventType == DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER || eventType == DoctorMaterialConsumeProvider.EVENT_TYPE.DIAORU)){
            throw new ServiceException("price.invalid");
        }
        // 盘盈, 查询最近一次入库单价
        if(eventType == DoctorMaterialConsumeProvider.EVENT_TYPE.PANYING){
            DoctorMaterialConsumeProvider lastCP = doctorMaterialConsumeProviderDao.findLastEvent(dto.getWareHouseId(), dto.getMaterialTypeId(), DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER);
            if(lastCP != null){
                dto.setUnitPrice(lastCP.getUnitPrice());
            }else{
                throw new ServiceException("warehouse.has.no.provider.event"); // 仓库没有入库事件
            }
        }

        MaterialFactory materialFactory = null;
        if(dto.getProviderFactoryId() != null){
            materialFactory = materialFactoryDao.findById(dto.getProviderFactoryId());
            if(materialFactory == null){
                throw new ServiceException("MaterialFactory.not.found");
            }
        }else if(StringUtils.isNotBlank(dto.getProviderFactoryName())){
            materialFactory = materialFactoryDao.findByFarmAndName(dto.getFarmId(), dto.getProviderFactoryName());
            if(materialFactory == null){
                materialFactory = new MaterialFactory();
                materialFactory.setFarmId(dto.getFarmId());
                materialFactory.setFarmName(dto.getFarmName());
                materialFactory.setFactoryName(dto.getProviderFactoryName());
                materialFactoryDao.create(materialFactory);
            }
        }

        DoctorMaterialConsumeProvider materialCP = DoctorMaterialConsumeProvider.buildFromDto(dto);
        materialCP.setProviderFactoryId(materialFactory == null ? null : materialFactory.getId());
        materialCP.setProviderFactoryName(materialFactory == null ? null : materialFactory.getFactoryName());
        doctorMaterialConsumeProviderDao.create(materialCP);
        doctorMaterialPriceInWareHouseDao.create(DoctorMaterialPriceInWareHouse.buildFromDto(dto, materialCP.getId()));
        context.put("eventId",materialCP.getId());
    }
}
