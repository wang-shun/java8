package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.service.DoctorImportDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/import/data")
public class DoctorImportDataController {

    @Autowired
    private DoctorImportDataService doctorImportDataService;

    /**
     * 导入所有的猪场数据
     * @param file excel文件
     * @return 是否成功
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public boolean importAll(MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            DoctorImportSheet sheet = new DoctorImportSheet();
            sheet.setFarm(getSheet(workbook, ""));
            sheet.setStaff(getSheet(workbook, ""));
            sheet.setBarn(getSheet(workbook, ""));
            sheet.setBreed(getSheet(workbook, ""));
            sheet.setSow(getSheet(workbook, ""));
            sheet.setBoar(getSheet(workbook, ""));
            sheet.setGroup(getSheet(workbook, ""));
            sheet.setWarehouse(getSheet(workbook, ""));
            sheet.setMedicine(getSheet(workbook, ""));
            sheet.setVacc(getSheet(workbook, ""));
            sheet.setMaterial(getSheet(workbook, ""));
            sheet.setFeed(getSheet(workbook, ""));
            sheet.setConsume(getSheet(workbook, ""));
            doctorImportDataService.importAll(sheet);
            return true;
        } catch (ServiceException e) {
            log.error("import all excel failed, file:{}", file.getName(), Throwables.getStackTraceAsString(e));
            return false;
        } catch (Exception e) {
            log.error("import all excel failed, file:{}", file.getName(), Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    private Sheet getSheet(Workbook wk, String name) {
        Sheet sheet = wk.getSheet(name);
        if (sheet == null) {
            throw new JsonResponseException("sheet.not.found");
        }
        return sheet;
    }
}
