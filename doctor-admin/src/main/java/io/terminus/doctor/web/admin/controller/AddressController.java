package io.terminus.doctor.web.admin.controller;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.parana.user.address.model.Address;
import io.terminus.parana.user.address.service.AddressReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/address")
public class AddressController {
    private final AddressReadService addressReadService;

    @Autowired
    public AddressController(AddressReadService addressReadService){
        this.addressReadService = addressReadService;
    }

    /**
     * 获取所有省份
     * @return 省份列表
     */
    @RequestMapping(value = "/provinces", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> provinces(){
        return RespHelper.or500(addressReadService.provinces());
    }

    /**
     * 某个省份的城市列表
     *
     * @param provinceId 省份id
     * @return 城市列表
     */
    @RequestMapping(value = "/province/{provinceId}/cities", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> cities(@PathVariable Integer provinceId){
        return RespHelper.or500(addressReadService.citiesOf(provinceId));
    }

    /**
     * 获取某城市的地区列表
     *
     * @param cityId 城市id
     * @return 地区列表
     */
    @RequestMapping(value = "/city/{cityId}/districts", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> districts(@PathVariable Integer cityId){
        return RespHelper.or500(addressReadService.regionsOf(cityId));
    }

    /**
     * 获取某地区的街道列表
     *
     * @param districtId 地区id
     * @return 地区列表
     */
    @RequestMapping(value = "/district/{districtId}/streets", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> streets(@PathVariable Integer districtId){
        return RespHelper.or500(addressReadService.streetsOf(districtId));
    }

    /**
     * 获取某一个地址的所有下级地址
     *
     * @param addressId id
     * @return 下级地址列表
     */
    @RequestMapping(value = "/address/{addressId}/children", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> children(@PathVariable Integer addressId){
        return RespHelper.or500(addressReadService.childAddressOf(addressId));
    }
}
