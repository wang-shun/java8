package io.terminus.doctor.web.core.export;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.web.core.export.property.ExportColumn;
import io.terminus.doctor.web.core.export.property.ExportTable;
import io.terminus.doctor.web.core.export.property.ExportTables;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by xjn on 16/12/29.
 */
@Slf4j
@Component
public class DefaultExporter implements Exporter{
    private final ColumnFormatterRegistry columnFormatterRegistry;
    private final ExportTables tables;

    @Autowired
    public DefaultExporter(ColumnFormatterRegistry columnFormatterRegistry, ExportTables tables) {
        this.columnFormatterRegistry = columnFormatterRegistry;
        this.tables = tables;
    }

    @Override
    public XSSFWorkbook export(List dataList, String exportName) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            ExportTable table = tables.getTables().get(exportName);
            XSSFSheet sheet = workbook.createSheet(table.getDisplay());
            createTitle(sheet, table);
            for (int i = 0; i < dataList.size(); i++) {
                createContext(sheet, table, i + 1, Params.objToMap(dataList.get(i)));
            }
        } catch (Exception e) {
            log.error("export fail, dataList:{}, exportName:{}, cause={}",
                    dataList, exportName, Throwables.getStackTraceAsString(e));
        }
        return workbook;
    }

    @Override
     public void export(List dataList, String exportName, HttpServletRequest request, HttpServletResponse response) {
        ExportTable table = tables.getTables().get(exportName);
        if (table == null) {
            log.error("download the excel of {} failed, cause={}", exportName, "table.config.missing");
            throw new JsonResponseException("table.config.missing");
        }
        try {
            setHttpServletResponse(request, response, table.getDisplay());
            XSSFWorkbook book = export(dataList, exportName);
            book.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("download the excel of{} failed, cause={}", exportName, Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public <T, C extends Map<String, String>> void export(String exportName, C criteria, Integer pageNo, Integer size, Function<C, Paging<T>> func, HttpServletRequest request, HttpServletResponse response) {

        ExportTable table = tables.getTables().get(exportName);
        if (table == null) {
            log.error("download the excel of {} failed, cause={}", exportName, "table.config.missing");
            throw new JsonResponseException("table.config.missing");
        }
        try {
            setHttpServletResponse(request, response, table.getDisplay());
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(table.getDisplay());

            //创建表头
            createTitle(sheet, table);

            //设置分页大小
            criteria.put("pageNo", pageNo.toString());
            criteria.put("size", size.toString());
            //body的开始行
            int index = 1;
            boolean hasNext = true;
            long max = 0L;

            //分页写入数据
            while (hasNext) {
                Paging resp = func.apply(criteria);
                if (Arguments.isNullOrEmpty(resp.getData())) {
                    break;
                }
                pageNo += pageNo;
                criteria.put("pageNo", pageNo.toString());
                //创建每一行
                for (Object obj: resp.getData()) {
                    createContext(sheet, table, index, Params.objToMap(obj));
                    index++;
                }

                //设置while退出条件
                max += resp.getData().size();
                if (max >= resp.getTotal()) {
                    hasNext = false;
                }
            }
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("export paging failed, exportName:{}, creiteria:{}, func:{}, cause:{}", exportName, criteria, func, Throwables.getStackTraceAsString(e));
        }
    }

    //创建表头
    private void createTitle(XSSFSheet sheet, ExportTable table) {
        Row titleRow = sheet.createRow(0);
        for (int i = 0; i < table.getColumns().size(); i++) {
            ExportColumn column = table.getColumns().get(i);
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(column.getDisplay());
            sheet.setColumnWidth(i, column.getWidth());
        }
    }

    //创建一行内容
    private void createContext(XSSFSheet sheet, ExportTable table, int index, Map<String, Object> fields) {
        Row row = sheet.createRow(index);

        //写入每一个cell
        for (int j = 0; j < table.getColumns().size(); j++) {
            ExportColumn column = table.getColumns().get(j);
            Cell cell = row.createCell(j);
            Object value = fields.get(column.getName());

            ColumnFormatter formatter = columnFormatterRegistry.getFormatter(column.getFormat());
            cell.setCellValue(formatter.format(value));
        }

    }

}
