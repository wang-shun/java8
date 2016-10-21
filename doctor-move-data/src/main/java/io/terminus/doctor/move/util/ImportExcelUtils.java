package io.terminus.doctor.move.util;

import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

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

}
