package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeAvgDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 物料消耗信息统计方式ReadService
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/12
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMaterialConsumeAvgReadServiceImpl implements DoctorMaterialConsumeAvgReadService {

    private final DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;
    private final DoctorWareHouseDao doctorWareHouseDao;
    private final DoctorMaterialInfoDao doctorMaterialInfoDao;
    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    @Autowired
    public DoctorMaterialConsumeAvgReadServiceImpl(DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao,
                                                   DoctorWareHouseDao doctorWareHouseDao,
                                                   DoctorMaterialInfoDao doctorMaterialInfoDao,
                                                   DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao) {
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
        this.doctorWareHouseDao = doctorWareHouseDao;
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
    }

    @Override
    public Response<List<DoctorMaterialConsumeAvgDto>> findMaterialConsumeAvgsByFarmId(Long farmId) {
        try{
            List<DoctorMaterialConsumeAvgDto> dtos = Lists.newArrayList();
            List<DoctorMaterialConsumeAvg> materialConsumeAvgs = doctorMaterialConsumeAvgDao.queryByIds(ImmutableMap.of("farmId", farmId));
            for (int i = 0; materialConsumeAvgs != null && i < materialConsumeAvgs.size(); i++) {
                DoctorMaterialConsumeAvg materialConsumeAvg = materialConsumeAvgs.get(i);
                DoctorMaterialConsumeAvgDto dmcAvgDto = BeanMapper.map(materialConsumeAvg, DoctorMaterialConsumeAvgDto.class);

                // 获取warehouse相关信息
                DoctorWareHouse wareHouse = doctorWareHouseDao.findById(materialConsumeAvg.getWareHouseId());
                if (wareHouse != null) {
                    dmcAvgDto.setFarmName(wareHouse.getFarmName());
                    dmcAvgDto.setWareHouseName(wareHouse.getWareHouseName());
                    dmcAvgDto.setManagerId(wareHouse.getManagerId());
                    dmcAvgDto.setManagerName(wareHouse.getManagerName());
                }

                // 获取物料名称
                DoctorMaterialInfo materialInfo = doctorMaterialInfoDao.findById(materialConsumeAvg.getMaterialId());
                if (materialInfo != null) {
                    dmcAvgDto.setMaterialName(materialInfo.getMaterialName());
                }

                // 获取剩余量信息
                DoctorMaterialInWareHouse materialInWareHouse = doctorMaterialInWareHouseDao.
                        queryByFarmHouseMaterial(
                                materialConsumeAvg.getFarmId(),
                                materialConsumeAvg.getWareHouseId(),
                                materialConsumeAvg.getMaterialId());
                if (materialInWareHouse != null) {
                    dmcAvgDto.setLotNumber(materialInWareHouse.getLotNumber());
                }

                dtos.add(dmcAvgDto);
            }
            return Response.ok(dtos);
        } catch (Exception e) {
            log.error("find MaterialConsumeAvgs by farmId failed, farmId:{}, cause by {}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("MaterialConsumeAvg.find.fail");
        }
    }

}
