package io.terminus.doctor.web.admin.basic.controller;

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

    @RequestMapping(value = "/provinces", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> provinces(){
        return RespHelper.or500(addressReadService.provinces());
    }

    @RequestMapping(value = "/province/{provinceId}/cities", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> cities(@PathVariable Integer provinceId){
        return RespHelper.or500(addressReadService.citiesOf(provinceId));
    }

    @RequestMapping(value = "/city/{cityId}/districts", method = RequestMethod.GET)
    @ResponseBody
    public List<Address> districts(@PathVariable Integer cityId){
        return RespHelper.or500(addressReadService.regionsOf(cityId));
    }
}
