package io.terminus.doctor.basic.handler.in;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.basic.dao.DoctorMaterialPriceInWareHouseDao;
import io.terminus.doctor.basic.dao.MaterialFactoryDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.handler.IHandler;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorMaterialPriceInWareHouse;
import io.terminus.doctor.basic.model.MaterialFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public boolean ifHandle(DoctorMaterialConsumeProvider.EVENT_TYPE eventType) {
        return eventType != null && eventType.isIn();
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException {
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
        context.setEventId(materialCP.getId());
    }

    @Override
    public void rollback(DoctorMaterialConsumeProvider cp) {
        DoctorMaterialPriceInWareHouse priceInWareHouse = doctorMaterialPriceInWareHouseDao.findByProviderId(cp.getId());
        if(priceInWareHouse == null || priceInWareHouse.getRemainder() < cp.getEventCount()){
            throw new ServiceException("provided.material.consumed"); // 本次入库物资已经出库, 无法回滚
        }
        doctorMaterialConsumeProviderDao.delete(cp.getId());
        doctorMaterialPriceInWareHouseDao.delete(priceInWareHouse.getId());
    }
}
