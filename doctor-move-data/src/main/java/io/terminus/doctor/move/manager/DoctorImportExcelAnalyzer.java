package io.terminus.doctor.move.manager;

import com.google.common.collect.Lists;
import io.terminus.doctor.move.dto.DoctorImportGroupEvent;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
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

    public List<DoctorImportPigEvent> getImportPigEvent(Sheet pigSheet) {
        List<DoctorImportPigEvent> importPigEventList = Lists.newArrayList();
        for (Row row : pigSheet) {
            if (row.getRowNum() > 0 && notEmpty(getString(row, 0))) {
                importPigEventList.add(transPigFromExcel(row));
            }
        }
        return importPigEventList;
    }

    public List<DoctorImportGroupEvent> getImportGroupEvent(Sheet groupSheet) {
        List<DoctorImportGroupEvent> importGroupEventList = Lists.newArrayList();
        for (Row row : groupSheet) {
            if (row.getRowNum() > 0 && notEmpty(getString(row, 0))) {
                importGroupEventList.add(transGroupFromExcel(row));
            }
        }
        return importGroupEventList;
    }

    private DoctorImportPigEvent transPigFromExcel(Row row) {
        return DoctorImportPigEvent.builder()
                .pigCode(getString(row, 0))
                .eventAt(getDate(row, 1))
                .eventName(getString(row, 2))
                .barnName(getString(row, 3))
                .pigSex(getString(row, 4))
                .remark(getString(row, 5))
                .birthday(getDate(row, 6))
                .source(getString(row, 7))
                .breedName(getString(row, 8))
                .breedTypeName(getString(row, 9))
                .parity(getInt(row, 10))
                .boarType(getString(row, 11))
                .mateType(getString(row, 12))
                .mateBoarCode(getString(row, 13))
                .mateOperator(getString(row, 14))
                .pregCheckResult(getString(row, 15))
                .toBarnName(getString(row, 16))
                .farrowingType(getString(row, 17))
                .birthNestAvg(getDouble(row, 18))
                .healthyCount(getInt(row, 19))
                .weakCount(getInt(row, 20))
                .partWeanPigletsCount(getInt(row, 21))
                .partWeanAvgWeight(getDouble(row, 22))
                .weanToBarn(getString(row, 23))
                .build();

    }

    private DoctorImportGroupEvent transGroupFromExcel(Row row) {
        return DoctorImportGroupEvent.builder()
                .groupCode(getString(row, 0))
                .eventAt(getDate(row, 1))
                .eventName(getString(row, 2))
                .remark(getString(row, 3))
                .newBarnName(getString(row, 4))
                .sexName(getString(row, 5))
                .breedName(getString(row, 6))
                .geneticName(getString(row, 7))
                .source(getString(row, 8))
                .inTypeName(getString(row, 9))
                .quantity(getInt(row, 10))
                .avgDayAge(getInt(row, 11))
                .avgWeight(getDouble(row, 12))
                .build();
    }
}
