package io.terminus.doctor.web.core.export;

import com.google.api.client.util.Charsets;
import io.terminus.common.model.Paging;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by xjn on 16/12/29.
 */
public interface Exporter {
    /**
     * 将指定列表中的数据, 导出为一个excel对象。
     * 在导出之前, 具体实现会要求在指定位置配置样式。
     * 在DefaultExporter中, 会要求在export.yml中配置样式。
     * @param dataList 数据列表
     * @param exportName config.yml 配置的名称
     * @return 待导出的excel表单
     */
     XSSFWorkbook export(List dataList, String exportName);

    /**
     * 将指定列表中的数据, 导出到客户端。
     * 在导出之前, 具体实现会要求在指定位置配置样式。
     * 在DefaultExporter中, 会要求在export.yaml中配置样式。
     * @param dataList 数据列表
     * @param exportName config.yml 配置的名称
     * @param request 请求实体
     * @param response 响应实体, 具体导出的数据会写入这个实体中。
     */
     void export(List dataList, String exportName, HttpServletRequest request, HttpServletResponse response);

    /**
     * 通过指定的分页读取方法, 自动调用并导出数据到客户端(每读一个分页，处理一发数据)
     * 在导出之前, 具体实现会要求在指定位置配置样式。
     * 在DefaultExporter中, 会要求在export.yaml中配置样式。
     * @param exportName config.yml 配置的名称
     * @param criteria 分页查询条件
     * @param func  分页查询方法
     * @param pageNo 页码
     * @param size 每页大小
     * @param request 请求实体
     * @param response 响应实体, 具体导出的数据会写入这个实体中。
     * @param <T> 导出元素的类型约束
     * @param <C> 查询条件的类型约束, OrderCriteria。
     */
     <T, C extends Map<String, String>> void export(String exportName, C criteria, Integer pageNo, Integer size, Function<C, Paging<T> > func,
                                               HttpServletRequest request, HttpServletResponse response) ;

        /**
         * 设置导出时的响应实体, 注意避免名称乱码问题。
         * @param request
         * @param response
         * @param fileName 文件名称
         * @throws UnsupportedEncodingException
         */
         default void setHttpServletResponse(HttpServletRequest request, HttpServletResponse response, String fileName) throws Exception{
            String file = fileName;
            file += ".xlsx";

            val userAgent = request.getHeader("USER-AGENT");

            String finalFileName;
            if (StringUtils.contains(userAgent, "MSIE")) {//IE浏览器
                finalFileName = URLEncoder.encode(file, "UTF8");
            } else if (StringUtils.contains(userAgent, "Mozilla")) {//google,火狐浏览器
                finalFileName = new String(file.getBytes(), Charsets.ISO_8859_1);
            } else {
                finalFileName = URLEncoder.encode(file, "UTF8");//其他浏览器
            }
            //这里设置一下让浏览器弹出下载提示框，而不是直接在浏览器中打开
            response.setHeader("Content-Disposition", "attachment; filename=\"" + finalFileName + "\"");
            response.setContentType("application/x-download");
        }
}
