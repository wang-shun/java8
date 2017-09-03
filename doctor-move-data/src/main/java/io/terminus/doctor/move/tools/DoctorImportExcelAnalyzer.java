package io.terminus.doctor.move.tools;

import com.google.common.collect.Lists;
import io.terminus.doctor.move.dto.DoctorImportBoar;
import io.terminus.doctor.move.dto.DoctorImportGroup;
import io.terminus.doctor.move.dto.DoctorImportSow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.move.util.ImportExcelUtils.*;


/**
 * Created by xjn on 17/8/25.
 * 导入excel解析器
 */
@Component
public class DoctorImportExcelAnalyzer {


    public List<DoctorImportBoar> getImportBoar(Sheet pigSheet) {
        List<DoctorImportBoar> importPigEventList = Lists.newArrayList();
        for (Row row : pigSheet) {
            if (row.getRowNum() > 0 && notEmpty(getString(row, 0))) {
                importPigEventList.add(transBoarFromExcel(row));
            }
        }
        return importPigEventList;
    }

    public List<DoctorImportSow> getImportSow(Sheet pigSheet) {
        List<DoctorImportSow> importPigEventList = Lists.newArrayList();
        for (Row row : pigSheet) {
            if (row.getRowNum() > 0 && notEmpty(getString(row, 0))) {
                importPigEventList.add(transSowFromExcel(row));
            }
        }
        return importPigEventList;
    }

    public List<DoctorImportGroup> getImportGroup(Sheet groupSheet) {
        List<DoctorImportGroup> importGroupEventList = Lists.newArrayList();
        for (Row row : groupSheet) {
            if (row.getRowNum() > 0 && notEmpty(getString(row, 0))) {
                importGroupEventList.add(transGroupFromExcel(row));
            }
        }
        return importGroupEventList;
    }

    private DoctorImportBoar transBoarFromExcel(Row row) {
        return DoctorImportBoar.builder()
                .barnName(getString(row, 0))
                .boarCode(getString(row, 1))
                .inFarmIn(getDate(row, 2))
                .birthday(getDate(row, 3))
                .fatherCode(getString(row, 4))
                .motherCOde(getString(row, 5))
                .breedName(getString(row, 6))
                .source(getString(row, 7))
                .boarType(getString(row, 8))
                .build();
    }

    private DoctorImportSow transSowFromExcel(Row row) {
        return DoctorImportSow.builder()
                .barnName(getString(row, 0))
                .sowCode(getString(row, 1))
                .currentStatus(getString(row, 2))
                .parity(getInt(row, 3))
                .mateDate(getDate(row, 4))
                .boarCode(getString(row, 5))
                .mateStaffName(getString(row, 6))
                .prePregDate(getDate(row, 7))
                .pregDate(getDate(row, 8))
                .farrowBarnName(getString(row, 9))
                .bed(getString(row, 10))
                .weanDate(getDate(row, 11))
                .liveCount(getInt(row, 12))
                .jixingCount(getInt(row, 13))
                .weakCount(getInt(row, 14))
                .deadCount(getInt(row, 15))
                .mummyCount(getInt(row, 16))
                .blackCount(getInt(row, 17))
                .nestWeight(getDouble(row, 18))
                .staff1(getString(row, 19))
                .staff2(getString(row, 20))
                .sowEarCode(getString(row, 21))
                .birthDate(getDate(row, 22))
                .remark(getString(row, 23))
                .breed(getString(row, 24))
                .weanWeight(getDouble(row, 25))
                .weakCount(getInt(row, 26))
                .fatherCode(getString(row, 27))
                .motherCode(getString(row, 28))
                .inFarmDate(getDate(row, 29))
                .build();
    }

    private DoctorImportGroup transGroupFromExcel(Row row) {
        return DoctorImportGroup.builder()
                .groupCode(getString(row, 0))
                .barnName(getString(row, 1))
                .sex(getString(row, 2))
                .liveStock(getInt(row, 3))
                .avgDayAge(getInt(row, 4))
                .avgWeight(getDouble(row, 5))
                .newGroupDate(getDate(row, 6))
                .build();
    }
}
