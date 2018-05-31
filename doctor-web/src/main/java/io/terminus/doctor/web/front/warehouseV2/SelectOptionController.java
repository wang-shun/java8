package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.service.warehouseV2.SelectOptionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * 选项Controller
 */
@RestController
@RequestMapping("api/doctor/warehouse/select")
public class SelectOptionController {

    @RpcConsumer
    private SelectOptionService selectOptionService;

    /**
     * 猪舍类型
     * @return
     */
    @RequestMapping(value = "/pigtype",method = RequestMethod.GET)
    public List<Map<String,String>> getPigTypeOption(){
        return selectOptionService.getPigTypeOption();
    }

    /**
     * 猪舍名称
     * @return
     */
    @RequestMapping(value = "/pigbarnname",method = RequestMethod.GET)
    public List<Map<String,Object>> getPigBarnNameOption(Long farmId,Integer pigType){
        return selectOptionService.getPigBarnNameOption(farmId,pigType);
    }

    /**
     * 猪群名称
     * @return
     */
    @RequestMapping(value = "/piggroupname",method = RequestMethod.GET)
    public List<Map<String,Object>> getPigGroupNameOption(Long farmId,Long barnId){
        return selectOptionService.getPigGroupNameOption(farmId,barnId);
    }

    /**
     * 事件类型
     * @return
     */
    @RequestMapping(value = "/handlertype",method = RequestMethod.GET)
    public List<Map<String,String>> getHandlerTypeOption(){
        return selectOptionService.getHandlerTypeOption();
    }

    /**
     * 物料类型
     * @return
     */
    @RequestMapping(value = "/skutype",method = RequestMethod.GET)
    public List<Map<String,String>> getSkuTypeOption(){
        return selectOptionService.getSkuTypeOption();
    }

    /**
     * 仓库名称
     * @return
     */
    @RequestMapping(value = "/warehousedata",method = RequestMethod.GET)
    public List<Map<String,Object>> getWareHouseDataOption(Long farmId){
        return selectOptionService.getWareHouseDataOption(farmId);
    }

}
