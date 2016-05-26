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
    public void testProviderMaterial(){
        Long total = 1000l;
        Long eachProvider = 50l;

        providerMaterial(eachProvider, total + eachProvider);
        providerMaterial(eachProvider * 2, total + eachProvider * 2);
    }

    @Test
    public void testConsumeMaterial(){
        Long total = 1000l;
        Long eachConsume = 50l;
        consumeMaterial(eachConsume, total-eachConsume);
        consumeMaterial(eachConsume * 2, total - eachConsume * 2);

        consumeMaterialNotFeed(eachConsume, total - eachConsume);
        consumeMaterialNotFeed(eachConsume * 2, total - eachConsume * 2);
    }

    private void providerMaterial(Long providerCount, Long providerLeft){
        DoctorMaterialConsumeProviderDto dto = buildProviderDtoInfo();
        Response<Long> response = doctorMaterialInWareHouseWriteService.providerMaterialInfo(dto);
        Assert.assertTrue(response.isSuccess());

        // validate type
        DoctorFarmWareHouseType type = doctorFarmWareHouseTypeDao.findByFarmIdAndType(12345l, WareHouseType.VACCINATION.getKey());
        Assert.assertEquals(type.getLogNumber(), providerLeft);

        // validate track info
        DoctorWareHouseTrack doctorWareHouseTrack = doctorWareHouseTrackDao.findById(3l);
        Assert.assertEquals(doctorWareHouseTrack.getLotNumber(), providerLeft);
        Assert.assertEquals(Params.getWithConvert(doctorWareHouseTrack.getExtraMap(),"3", a->Long.valueOf(a.toString())), providerLeft);

        // validate in ware house
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(12345l, 3l, 3l);
        Assert.assertEquals(doctorMaterialInWareHouse.getLotNumber(), providerLeft);

        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider =  doctorMaterialConsumeProviderDao.findById(response.getResult());
        Assert.assertEquals(doctorMaterialConsumeProvider.getFarmId(), new Long(12345l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getMaterialId(), new Long(3l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getWareHouseId(), new Long(3l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getEventCount(), new Long(50l));
    }

    /**
     * 测试非饲料 物料的消耗的方式
     * @param consumeCount
     * @param consumeLeft
     */
    private void consumeMaterialNotFeed(Long consumeCount, Long consumeLeft){
        // consume material
        DoctorMaterialConsumeProviderDto dto = buildConsumerProvider();
        dto.setType(WareHouseType.MEDICINE.getKey());
        dto.setMaterialTypeId(2l);  // 消耗对应原料类型2 原料信息
        dto.setWareHouseId(2l);
        Response<Long> response = doctorMaterialInWareHouseWriteService.consumeMaterialInfo(dto);
        Assert.assertTrue(response.isSuccess());

        // validate type
        DoctorFarmWareHouseType type = doctorFarmWareHouseTypeDao.findByFarmIdAndType(12345l, WareHouseType.MEDICINE.getKey());
        Assert.assertEquals(type.getLogNumber(),consumeLeft);

        // validate track warehouse
        DoctorWareHouseTrack doctorWareHouseTrack = doctorWareHouseTrackDao.findById(2l);
        Assert.assertEquals(doctorWareHouseTrack.getLotNumber(), consumeLeft);
        Assert.assertEquals(Params.getWithConvert(doctorWareHouseTrack.getExtraMap(), "2", a->Long.valueOf(a.toString())), consumeLeft);

        // validate in ware house count
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(12345l, 2l, 2l);
        Assert.assertEquals(doctorMaterialInWareHouse.getLotNumber(), consumeLeft);

        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider =  doctorMaterialConsumeProviderDao.findById(response.getResult());
        Assert.assertEquals(doctorMaterialConsumeProvider.getFarmId(), new Long(12345l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getMaterialId(), new Long(2l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getWareHouseId(), new Long(2l));
        Assert.assertEquals(doctorMaterialConsumeProvider.getEventCount(), new Long(50l));

        DoctorMaterialConsumeAvg doctorMaterialConsumeAvg = doctorMaterialConsumeAvgDao.queryByIds(12345l, 2l, 2l);
        Assert.assertEquals(doctorMaterialConsumeAvg.getConsumeCount(), consumeCount); // breed Consume 每次相同的
        Assert.assertEquals(doctorMaterialConsumeAvg.getConsumeAvgCount(), new Long(0));
        Assert.assertEquals(doctorMaterialConsumeAvg.getConsumeDate().getTime(), DateTime.now().withTimeAtStartOfDay().toDate().getTime());
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

    /**
     * 构建对应的Provider 数据信息
     * @return
     */
    private DoctorMaterialConsumeProviderDto buildProviderDtoInfo(){

        DoctorMaterialConsumeProviderDto dto = new DoctorMaterialConsumeProviderDto();
        dto.setType(WareHouseType.VACCINATION.getKey());
        dto.setFarmId(12345l);
        dto.setFarmName("farmName");
        dto.setWareHouseId(3l);
        dto.setWareHouseName("wareHouseName");
        dto.setMaterialTypeId(3l);
        dto.setMaterialName("materialName");
        dto.setProviderCount(50l);
        return dto;

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
