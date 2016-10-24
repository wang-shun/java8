package io.terminus.doctor.move.util;

import com.google.common.base.Strings;
import io.terminus.doctor.common.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Date;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */

public class ImportExcelUtils {

    public static String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        cell.setCellType(Cell.CELL_TYPE_STRING);
        return cell.getStringCellValue();
    }

    public static String getStringOrThrow(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            throw new IllegalStateException("row " + row.getRowNum() + "column " + col + " is null");
        }
        cell.setCellType(Cell.CELL_TYPE_STRING);
        String value = cell.getStringCellValue();
        if(StringUtils.isBlank(value)){
            throw new IllegalStateException("row " + row.getRowNum() + "column " + col + " is blank");
        }
        return value;
    }

    public static Long getLong(Row row, int col) {
        String value = getString(row, col);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Long.parseLong(value);
    }

    public static Integer getInt(Row row, int col) {
        String value = getString(row, col);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public static Integer getIntOrDefault(Row row, int col, int defaultValue) {
        String value = getString(row, col);
        return notEmpty(value) ? Integer.parseInt(value) : defaultValue;
    }

    public static Double getDouble(Row row, int col) {
        String value = getString(row, col);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Double.parseDouble(value);
    }

    public static Double getDoubleOrDefault(Row row, int col, double defaultValue) {
        String value = getString(row, col);
        return notEmpty(value) ? Double.parseDouble(value) : defaultValue;
    }

    public static Integer getPrice(Row row, int col) {
        Double value = getDouble(row, col);
        if (value == null) {
            return null;
        }
        return (int)((value + Double.MIN_VALUE) * 100);
    }

    public static Date getDate(Row row, int col){
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        if(cell.getCellType() == Cell.CELL_TYPE_STRING){
            return DateUtil.formatToDate(DateUtil.DATE_SLASH, cell.getStringCellValue());
        }else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
            return cell.getDateCellValue();
        }else{
            return null;
        }
    }

}
