package io.terminus.doctor.web.front.warehouseV2;

import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrgsLogs;
import io.terminus.doctor.user.service.DoctorOrgsLogsReadService;
import io.terminus.doctor.user.service.DoctorOrgsLogsWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-06-13 19:41:03
 * Created by [ your name ]
 */
@Slf4j
@RestController
@RequestMapping
public class DoctorOrgsLogss {

    @RpcConsumer
    private DoctorOrgsLogsWriteService doctorOrgsLogsWriteService;

    @RpcConsumer
    private DoctorOrgsLogsReadService doctorOrgsLogsReadService;

    /**
     * 查询
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorOrgsLogs findDoctorOrgsLogs(@PathVariable Long id) {
        Response<DoctorOrgsLogs> response =  doctorOrgsLogsReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<DoctorOrgsLogs> pagingDoctorOrgsLogs(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        Map<String, Object> criteria = Maps.newHashMap();

        Response<Paging<DoctorOrgsLogs>> result =  doctorOrgsLogsReadService.paging(pageNo, pageSize, criteria);
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

    /**
     * 创建
     * @param doctorOrgsLogs
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createDoctorOrgsLogs(@RequestBody DoctorOrgsLogs doctorOrgsLogs) {
        Response<Long> response = doctorOrgsLogsWriteService.create(doctorOrgsLogs);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 更新
     * @param doctorOrgsLogs
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateDoctorOrgsLogs(@RequestBody DoctorOrgsLogs doctorOrgsLogs) {
        Response<Boolean> response = doctorOrgsLogsWriteService.update(doctorOrgsLogs);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
    * 删除
    * @param id
    * @return
    */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean deleteDoctorOrgsLogs(@PathVariable Long id) {
        Response<Boolean> response = doctorOrgsLogsWriteService.delete(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

   /**
    * 列表
    * @return
    */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorOrgsLogs> listDoctorOrgsLogs() {
       Map<String, Object> criteria = Maps.newHashMap();
       Response<List<DoctorOrgsLogs>> result =  doctorOrgsLogsReadService.list(criteria);
       if(!result.isSuccess()){
           throw new JsonResponseException(result.getError());
       }
       return result.getResult();
    }
}