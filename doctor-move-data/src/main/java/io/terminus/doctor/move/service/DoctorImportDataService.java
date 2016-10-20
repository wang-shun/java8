package io.terminus.doctor.move.service;

import com.google.common.collect.Lists;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.util.ImportExcelUtils;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */
@Slf4j
@Service
public class DoctorImportDataService {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy/MM/dd");

    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorBasicDao doctorBasicDao;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorMoveBasicService doctorMoveBasicService;

    /**
     * 根据shit导入所有的猪场数据
     */
    public void importAll(DoctorImportSheet shit) {
        DoctorFarm farm = new DoctorFarm();
        Map<String, Long> userMap = doctorMoveBasicService.getSubMap(farm.getOrgId());

        importBarn(farm, userMap, shit.getBarn());

        Map<String, DoctorBarn> barnMap = doctorMoveBasicService.getBarnMap2(farm.getId());
        Map<String, Long> breedMap = doctorMoveBasicService.getBreedMap();
    }

    public DoctorFarm importOrgFarmUser(Sheet shit) {
        return new DoctorFarm();
    }

    /**
     * 导入猪舍
     */
    public void importBarn(DoctorFarm farm, Map<String, Long> userMap, Sheet shit) {
        List<DoctorBarn> barns = Lists.newArrayList();
        shit.forEach(row -> {
            //第一行是表头，跳过
            if (canImport(row)) {
                DoctorBarn barn = new DoctorBarn();
                barn.setName(ImportExcelUtils.getString(row, 0));
                barn.setOrgId(farm.getOrgId());
                barn.setOrgName(farm.getOrgName());
                barn.setFarmId(farm.getId());
                barn.setFarmName(farm.getName());

                PigType pigType = PigType.from(ImportExcelUtils.getString(row, 1));
                if (pigType != null) {
                    barn.setPigType(pigType.getValue());
                } else {
                    log.error("farm:{}, barn:{} type is null, please check!", farm, barn.getName());
                }

                barn.setCanOpenGroup(DoctorBarn.CanOpenGroup.YES.getValue());
                barn.setStatus(DoctorBarn.Status.USING.getValue());
                barn.setCapacity(1000);
                barn.setStaffName(ImportExcelUtils.getString(row, 3));
                barn.setStaffId(userMap.get(barn.getStaffName()));
                barn.setExtra(ImportExcelUtils.getString(row, 4));
                barns.add(barn);
            }
        });
        doctorBarnDao.creates(barns);
    }

    public void importBreed(Sheet shit) {
        List<String> breeds = doctorBasicDao.findByType(DoctorBasic.Type.BREED.getValue()).stream()
                .map(DoctorBasic::getName).collect(Collectors.toList());
        shit.forEach(row -> {
            if (canImport(row)) {
                String breedName = ImportExcelUtils.getString(row, 0);
                if (!breeds.contains(breedName)) {
                    DoctorBasic basic = new DoctorBasic();
                    basic.setName(breedName);
                    basic.setType(DoctorBasic.Type.BREED.getValue());
                    basic.setTypeName(DoctorBasic.Type.BREED.getDesc());
                    basic.setIsValid(1);
                    basic.setSrm(ImportExcelUtils.getString(row, 1));
                    doctorBasicDao.create(basic);
                }
            }
        });
    }

    /**
     * 导入公猪
     */
    public void importBoar(DoctorFarm farm, Map<String, Long> barnMap, Map<String, Long> breedMap, Sheet shit) {
        for (Row row : shit) {
            if (!canImport(row)) {
                continue;
            }

            DoctorPig boar = new DoctorPig();
            boar.setOrgId(farm.getOrgId());
            boar.setOrgName(farm.getOrgName());
            boar.setFarmId(farm.getId());
            boar.setFarmName(farm.getName());
            boar.setPigCode(ImportExcelUtils.getString(row, 1));
            boar.setPigType(PigType.BOAR.getValue());
            boar.setIsRemoval(IsOrNot.NO.getValue());
            boar.setPigFatherCode(ImportExcelUtils.getString(row, 4));
            boar.setPigMotherCode(ImportExcelUtils.getString(row, 5));
            PigSource source = PigSource.from(ImportExcelUtils.getString(row, 7));
            if (source != null) {
                boar.setSource(source.getKey());
            }
            boar.setBirthDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 3)));
            boar.setInFarmDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 2)));
            boar.setInitBarnName(ImportExcelUtils.getString(row, 0));
            boar.setInitBarnId(barnMap.get(boar.getInitBarnName()));
            boar.setBreedName(ImportExcelUtils.getString(row, 6));
            boar.setBreedId(breedMap.get(boar.getBreedName()));
            doctorPigDao.create(boar);

            DoctorPigTrack boarTrack = new DoctorPigTrack();
            boarTrack.setFarmId(boar.getFarmId());
            boarTrack.setPigId(boar.getId());
            boarTrack.setPigType(boar.getPigType());
            boarTrack.setStatus(PigStatus.BOAR_ENTRY.getKey());
            boarTrack.setIsRemoval(boar.getIsRemoval());
            boarTrack.setCurrentBarnId(boar.getInitBarnId());
            boarTrack.setCurrentBarnName(boar.getInitBarnName());
            boarTrack.setCurrentParity(1);      //配种次数置成1
            doctorPigTrackDao.create(boarTrack);
        }
    }

    public void importGroup(DoctorFarm farm, Sheet shit) {

    }

    public void importSow(DoctorFarm farm, Sheet shit) {

    }

    public void importWarehouse(DoctorFarm farm, Sheet shit) {

    }

    public void importMedicine(DoctorFarm farm, Sheet shit) {

    }

    public void importVacc(DoctorFarm farm, Sheet shit) {

    }

    public void importMaterial(DoctorFarm farm, Sheet shit) {

    }

    public void importFeed(DoctorFarm farm, Sheet shit) {

    }

    public void importConsume(DoctorFarm farm, Sheet shit) {

    }

    //第一行是表头，跳过  第一列不能为空
    private static boolean canImport(Row row) {
        return row.getRowNum() > 0 && notEmpty(ImportExcelUtils.getString(row, 0));
    }
}
