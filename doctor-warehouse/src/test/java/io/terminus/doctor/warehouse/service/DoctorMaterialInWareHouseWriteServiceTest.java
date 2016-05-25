package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.enums.WareHouseType;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 测试 原料的提供，获取操作方式
 */
public class DoctorMaterialInWareHouseWriteServiceTest extends BasicServiceTest{

    @Autowired
    private DoctorMaterialInWareHouseWriteService doctorMaterialInWareHouseWriteService;

    @Autowired
    private DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    @Autowired
    private DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    @Autowired
    private DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    @Autowired
    private DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    private DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;

    @Before
    public void testCondition(){
        Assert.assertNotNull(doctorMaterialInWareHouseWriteService);
    }

    @Test
    public void testConsumeMaterial(){
        Long total = 1000l;
        Long eachConsume = 50l;
        consumeMaterial(eachConsume, total-eachConsume);
        consumeMaterial(eachConsume * 2, total - eachConsume * 2);
    }

    /**
     * consume material  消耗对应的 原料信息
     * @param consumeCount  消耗的物料的数量信息
     * @param consumeLeft   剩余的物料数量信息
     */
    private void consumeMaterial(Long consumeCount, Long consumeLeft){
        // first consumer
        Response<Long> response = doctorMaterialInWareHouseWriteService.consumeMaterialInfo(buildConsumerProvider());
        Assert.assertTrue(response.isSuccess());

        // validate type
        DoctorFarmWareHouseType type = doctorFarmWareHouseTypeDao.findByFarmIdAndType(12345l, WareHouseType.FEED.getKey());
        Assert.assertEquals(type.getLogNumber(),consumeLeft);

        // validate track warehouse
        DoctorWareHouseTrack doctorWareHouseTrack = doctorWareHouseTrackDao.findById(1l);
        Assert.assertEquals(doctorWareHouseTrack.getLotNumber(), consumeLeft);
        Assert.assertEquals(Params.getWithConvert(doctorWareHouseTrack.getExtraMap(), "1", a->Long.valueOf(a.toString())), consumeLeft);

        // validate in ware house count
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(12345l, 1l, 1l);
        Assert.assertEquals(doctorMaterialInWareHouse.getLotNumber(), consumeLeft);

        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider =  doctorMaterialConsumeProviderDao.findById(response.getResult());
        Assert.assertEquals(doctorMaterialConsumeProvider.getFarmId(), new Long(12345l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getMaterialId(), new Long(1l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getWareHouseId(), new Long(1l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getEventCount(), new Long(50l));

        DoctorMaterialConsumeAvg doctorMaterialConsumeAvg = doctorMaterialConsumeAvgDao.queryByIds(12345l, 1l, 1l);
        Assert.assertEquals(doctorMaterialConsumeAvg.getConsumeCount(), new Long(50l)); // breed Consume 每次相同的
        Assert.assertEquals(doctorMaterialConsumeAvg.getConsumeAvgCount(), new Long(0));
        Assert.assertEquals(doctorMaterialConsumeAvg.getConsumeDate().getTime(), DateTime.now().withTimeAtStartOfDay().toDate().getTime());
    }

    private DoctorMaterialConsumeProviderDto buildConsumerProvider(){
        DoctorMaterialConsumeProviderDto dto = new DoctorMaterialConsumeProviderDto();
        dto.setType(WareHouseType.FEED.getKey());
        dto.setFarmId(12345l);
        dto.setFarmName("farmName");
        dto.setWareHouseId(1l);
        dto.setWareHouseName("wareHouseName");
        dto.setMaterialTypeId(1l);
        dto.setMaterialName("materialName");
        dto.setBarnId(1l);
        dto.setBarnName("barnName");
        dto.setStaffId(1l);
        dto.setStaffName("staffName");
        dto.setConsumeCount(50l);
        dto.setConsumeDays(100);
        return dto;
    }
}
