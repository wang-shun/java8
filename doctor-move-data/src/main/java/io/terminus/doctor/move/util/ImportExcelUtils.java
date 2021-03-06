package io.terminus.doctor.move.util;

import com.google.common.base.Strings;
import io.terminus.doctor.common.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
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
        return cell.getStringCellValue().trim();
    }

    public static String getStringOrThrow(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            throw new IllegalStateException("页名: " + row.getSheet().getSheetName() + "行号 " + (row.getRowNum() + 1) + "列号 " + (col + 1) + " 不存在");
        }
        cell.setCellType(Cell.CELL_TYPE_STRING);
        String value = cell.getStringCellValue().trim();
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException("页名: " + row.getSheet().getSheetName() + "行号 " + (row.getRowNum() + 1) + "列号 " + (col + 1) + " 为空");
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

        if (!NumberUtils.isDigits(value))
            throw new IllegalStateException("页名: " + row.getSheet().getSheetName() +
                    ",行号: " + (row.getRowNum() + 1) +
                    ",列号: " + (col + 1) +
                    ",值为: " + value + ",不是有效的整数");

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

        if (!NumberUtils.isNumber(value))
            throw new IllegalStateException("页名: " + row.getSheet().getSheetName() +
                    ",行号: " + (row.getRowNum() + 1) +
                    ",列号: " + (col + 1) +
                    ",值为: " + value + ",不是有效的数字");

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
        return (int) ((value + Double.MIN_VALUE) * 100);
    }

    public static Date getDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {

            return DateUtil.formatToDate(DateUtil.DATE_SLASH, cell.getStringCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return cell.getDateCellValue();
        } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        } else {
            return null;
        }
    }

}
