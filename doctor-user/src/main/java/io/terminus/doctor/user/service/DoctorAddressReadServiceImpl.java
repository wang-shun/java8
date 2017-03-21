package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.user.dao.DoctorAddressDao;
import io.terminus.doctor.user.dto.DoctorAddressDto;
import io.terminus.parana.user.address.model.Address;
import io.terminus.parana.user.impl.address.service.AddressReadServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/14
 */
@Slf4j
@Service
@RpcProvider
public class DoctorAddressReadServiceImpl implements DoctorAddressReadService {

    private final DoctorAddressDao addressDao;

    private List<DoctorAddressDto> addressTree;

    private static final int MAX_DEPTH = 3;     //递归寻址最大深度(目前取值为最大level)

    @Autowired
    public DoctorAddressReadServiceImpl(DoctorAddressDao addressDao) {
        this.addressDao = addressDao;
    }

    @PostConstruct
    public void loadAddress() throws InterruptedException {
        addressTree = Lists.newArrayList();
        //初始 pid = 1
        new Thread(() -> {
            log.info("build address tree start, now:{}", DateUtil.toDateTimeString(new Date()));
            addressTree = getAddressTreeByPid(1);
            log.info("build address tree end, now:{}", DateUtil.toDateTimeString(new Date()));
        }).start();
    }

    @Override
    public Response<List<DoctorAddressDto>> findAllAddress() {
        try {
            return Response.ok(addressTree);
        } catch (Exception e) {
            log.error("find all address failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("address.find.fail");
        }
    }

    //递归调用查询出地址树, 递归终止条件: level > MAX_DEPTH
    private List<DoctorAddressDto> getAddressTreeByPid(Integer pid) {
        log.debug("build tree start, pid: {}", pid);
        List<Address> addresses = addressDao.findByPid(pid);
        List<DoctorAddressDto> trees = Lists.newArrayListWithCapacity(addresses.size());

        addresses.forEach(address -> {
            if (address.getLevel() <= MAX_DEPTH) {
                DoctorAddressDto tree = new DoctorAddressDto();
                tree.setValue(address.getId());
                tree.setLabel(address.getName());

                List<DoctorAddressDto> childs = getAddressTreeByPid(address.getId());
                if (notEmpty(childs)) {         //如果为空,不显示子节点
                    tree.setChildren(childs);
                }
                trees.add(tree);
            }
        });
        return trees;
    }

    @Override
    public Response<Address> findByNameAndPid(String name, Integer pid){
        try {
            return Response.ok(addressDao.findByNameAndPid(name, pid));
        } catch (Exception e) {
            log.error("find address failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("address.find.fail");
        }
    }
}
