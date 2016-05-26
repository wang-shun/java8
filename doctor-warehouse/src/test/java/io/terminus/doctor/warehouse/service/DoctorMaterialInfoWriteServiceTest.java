package io.terminus.doctor.warehouse.service;

import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe: 测试对应的Material 原料的消耗过程
 */
public class DoctorMaterialInfoWriteServiceTest extends BasicServiceTest {

    @Autowired
    private DoctorMaterialInfoWriteService doctorMaterialInfoWriteService;

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

    /**
     * 测试对应的物料生产情况
     */
    @Test
    public void testProduceContent(){
        Response<Boolean> response = doctorMaterialInfoWriteService.realProduceMaterial(buildBasic(), buildProduce());
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());

        // validate type
        DoctorFarmWareHouseType type = doctorFarmWareHouseTypeDao.findByFarmIdAndType(12345l, 2);
        Assert.assertEquals(type.getLogNumber(), new Long(500));
        DoctorFarmWareHouseType type1 = doctorFarmWareHouseTypeDao.findByFarmIdAndType(12345l, 4);
        Assert.assertEquals(type.getLogNumber(), new Long(500));
        DoctorFarmWareHouseType type2 = doctorFarmWareHouseTypeDao.findByFarmIdAndType(12345l, 3);
        Assert.assertEquals(type2.getLogNumber(), new Long(2500));

        // validate track
        DoctorWareHouseTrack track = doctorWareHouseTrackDao.findById(1l);
        Assert.assertEquals(track.getLotNumber(),new Long(0l));
        Assert.assertEquals(Params.getWithConvert(track.getExtraMap(), "2", a->Long.valueOf(a.toString())),new Long(0l));
        DoctorWareHouseTrack track1 = doctorWareHouseTrackDao.findById(2l);
        Assert.assertEquals(track1.getLotNumber(),new Long(500l));
        Assert.assertEquals(Params.getWithConvert(track1.getExtraMap(), "2", a->Long.valueOf(a.toString())),new Long(500l));

        DoctorWareHouseTrack track3 = doctorWareHouseTrackDao.findById(4l);
        Assert.assertEquals(track3.getLotNumber(),new Long(0l));
        Assert.assertEquals(Params.getWithConvert(track3.getExtraMap(), "4", a->Long.valueOf(a.toString())),new Long(0l));
        DoctorWareHouseTrack track4 = doctorWareHouseTrackDao.findById(5l);
        Assert.assertEquals(track4.getLotNumber(),new Long(500l));
        Assert.assertEquals(Params.getWithConvert(track4.getExtraMap(), "4", a->Long.valueOf(a.toString())),new Long(500l));

        DoctorWareHouseTrack track5 = doctorWareHouseTrackDao.findById(3l);
        Assert.assertEquals(track5.getLotNumber(),new Long(2500l));
        Assert.assertEquals(Params.getWithConvert(track5.getExtraMap(), "3", a->Long.valueOf(a.toString())),new Long(2500l));

        // validate in house
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.findById(1l);
        Assert.assertEquals(doctorMaterialInWareHouse.getLotNumber(), new Long(0));
        DoctorMaterialInWareHouse doctorMaterialInWareHouse1 = doctorMaterialInWareHouseDao.findById(2l);
        Assert.assertEquals(doctorMaterialInWareHouse1.getLotNumber(), new Long(500));
        DoctorMaterialInWareHouse doctorMaterialInWareHouse2 = doctorMaterialInWareHouseDao.findById(3l);
        Assert.assertEquals(doctorMaterialInWareHouse2.getLotNumber(), new Long(2500));
        DoctorMaterialInWareHouse doctorMaterialInWareHouse3 = doctorMaterialInWareHouseDao.findById(4l);
        Assert.assertEquals(doctorMaterialInWareHouse3.getLotNumber(), new Long(0));
        DoctorMaterialInWareHouse doctorMaterialInWareHouse4 = doctorMaterialInWareHouseDao.findById(5l);
        Assert.assertEquals(doctorMaterialInWareHouse4.getLotNumber(), new Long(500));

        // validate in house avg
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialConsumeAvgDao.listAll()));

        // validate event
        List<DoctorMaterialConsumeProvider> doctorMaterialConsumeProviderList = doctorMaterialConsumeProviderDao.listAll();
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialConsumeProviderList));
    }

    private DoctorMaterialInfo.MaterialProduce buildProduce(){
        DoctorMaterialInfo.MaterialProduce produce = new DoctorMaterialInfo.MaterialProduce();
        produce.setTotal(1500l);
        produce.setMaterialProduceEntries(Lists.newArrayList(new DoctorMaterialInfo.MaterialProduceEntry(2l,"materialName", 1500l, 100d)));
        produce.setMedicalProduceEntries(Lists.newArrayList(new DoctorMaterialInfo.MaterialProduceEntry(4l, "medicalName", 1500l, 100d)));
        return produce;
    }

    private DoctorWareHouseBasicDto buildBasic(){
        DoctorWareHouseBasicDto dto = new DoctorWareHouseBasicDto();
        dto.setFarmId(12345l);
        dto.setFarmName("farmName");

        // 原料的类型 等同于 WareHouse 数据类型
        dto.setMaterialId(3l);
        dto.setMaterialName("materialName");
        dto.setWareHouseId(3l);
        dto.setMaterialName("materialName");
        dto.setStaffId(1l);
        dto.setStaffName("staffName");
        return dto;
    }
}
