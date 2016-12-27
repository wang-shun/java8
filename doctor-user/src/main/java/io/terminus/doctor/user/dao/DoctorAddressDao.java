package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.parana.user.address.model.Address;
import io.terminus.parana.user.impl.address.dao.AddressDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class DoctorAddressDao extends AddressDao {

    public Address findByNameAndPid(String name, Integer pid){
        return sqlSession.selectOne(sqlId("findByNameAndPid"), ImmutableMap.of("name", name, "pid", pid));
    }

}
