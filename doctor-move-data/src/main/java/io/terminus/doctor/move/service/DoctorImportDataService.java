package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.util.ImportExcelUtils;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
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
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
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

        importBoar(farm, barnMap, breedMap, shit.getBoar());

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
    public void importBoar(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Map<String, Long> breedMap, Sheet shit) {
        for (Row row : shit) {
            if (!canImport(row)) {
                continue;
            }

            //公猪
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
            DoctorBarn barn = barnMap.get(boar.getInitBarnName());
            if (barn != null) {
                boar.setInitBarnId(barn.getId());
            }
            boar.setBreedName(ImportExcelUtils.getString(row, 6));
            boar.setBreedId(breedMap.get(boar.getBreedName()));
            doctorPigDao.create(boar);

            //公猪跟踪
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

    /**
     * 导入猪群
     */
    public void importGroup(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Sheet shit) {
        for (Row row : shit) {
            if (!canImport(row)) {
                continue;
            }

            //猪群
            DoctorGroup group = new DoctorGroup();
            group.setOrgId(farm.getOrgId());
            group.setOrgName(farm.getOrgName());
            group.setFarmId(farm.getId());
            group.setFarmName(farm.getName());
            group.setGroupCode(ImportExcelUtils.getString(row, 0));
            group.setOpenAt(null);  // TODO: 2016/10/20 建群时间待定
            group.setStatus(DoctorGroup.Status.CREATED.getValue());
            group.setInitBarnName(ImportExcelUtils.getString(row, 1));

            DoctorBarn barn = barnMap.get(group.getInitBarnName());
            if (barn != null) {
                group.setInitBarnId(barn.getId());
                group.setPigType(barn.getPigType());
                group.setStaffId(barn.getStaffId());
                group.setStaffName(barn.getStaffName());
            }
            group.setCurrentBarnId(group.getInitBarnId());
            group.setCurrentBarnName(group.getInitBarnName());
            doctorGroupDao.create(group);

            //猪群跟踪
            DoctorGroupTrack groupTrack = new DoctorGroupTrack();
            groupTrack.setGroupId(group.getId());

            DoctorGroupTrack.Sex sex = DoctorGroupTrack.Sex.from(ImportExcelUtils.getString(row, 2));
            if (sex != null) {
                groupTrack.setSex(sex.getValue());
            }
            groupTrack.setQuantity(ImportExcelUtils.getInt(row, 3));
            groupTrack.setBoarQty(0);
            groupTrack.setSowQty(groupTrack.getQuantity() - groupTrack.getBoarQty());
            groupTrack.setAvgDayAge(ImportExcelUtils.getInt(row, 4));
            groupTrack.setBirthDate(DateTime.now().minusDays(groupTrack.getAvgDayAge()).toDate());
            groupTrack.setWeight(ImportExcelUtils.getDouble(row, 5));
            groupTrack.setAvgWeight(MoreObjects.firstNonNull(groupTrack.getWeight(), 0D) / groupTrack.getQuantity());
            groupTrack.setWeanAvgWeight(0D);
            groupTrack.setBirthAvgWeight(0D);
            groupTrack.setWeakQty(0);
            groupTrack.setUnweanQty(0);
            groupTrack.setUnqQty(0);
            doctorGroupTrackDao.create(groupTrack);
        }
    }

    /**
     * 导入母猪
     */
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
