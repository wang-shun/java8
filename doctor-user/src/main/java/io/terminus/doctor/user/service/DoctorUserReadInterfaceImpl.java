package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Params;
import io.terminus.doctor.user.interfaces.service.DoctorUserReadInterface;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.model.Paging;
import io.terminus.doctor.user.interfaces.model.Response;
import io.terminus.doctor.user.interfaces.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 陈增辉, 对外提供的用户模块的dubbo接口的实现类
 */
@Slf4j
@Service
public class DoctorUserReadInterfaceImpl implements DoctorUserReadInterface {

    private final UserDaoExt userDaoExt;


    @Autowired
    public DoctorUserReadInterfaceImpl(UserDaoExt userDaoExt){
        this.userDaoExt = userDaoExt;
    }

    @Override
    public Response<User> findByNick(String nickname) {
        Response<User> response = new Response<>();
        User user = new User();
        try {
            BeanMapper.copy(userDaoExt.findByName(nickname), user);
            response.setResult(this.setUserType(user));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<User> findByEmail(String email) {
        Response<User> response = new Response<>();
        User user = new User();
        try {
            BeanMapper.copy(userDaoExt.findByEmail(email), user);
            response.setResult(this.setUserType(user));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<User> findByMobile(String mobile) {
        Response<User> response = new Response<>();
        User user = new User();
        try {
            BeanMapper.copy(userDaoExt.findByMobile(mobile), user);
            response.setResult(this.setUserType(user));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<User> load(Integer id) {
        Response<User> response = new Response<>();
        User user = new User();
        try {
            BeanMapper.copy(userDaoExt.findById(id), user);
            response.setResult(this.setUserType(user));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<User> load(Long id) {
        Response<User> response = new Response<>();
        User user = new User();
        try {
            BeanMapper.copy(userDaoExt.findById(id), user);
            response.setResult(this.setUserType(user));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<List<User>> loads(List<Long> ids) {
        Response<List<User>> response = new Response<>();
        try {
            List<User> list = BeanMapper.mapList(userDaoExt.findByIds(ids), User.class);
            response.setResult(list);
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<List<User>> loads(Long id0, Long id1, Long... idn) {
        Response<List<User>> response = new Response<>();
        try {
            List<User> list = BeanMapper.mapList(userDaoExt.findByIds(id0, id1, idn), User.class);
            response.setResult(list);
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<List<User>> loadsBy(Map<String, Object> criteria) {
        Response<List<User>> response = new Response<>();
        try {
            List<User> list = BeanMapper.mapList(userDaoExt.list(Params.filterNullOrEmpty(criteria)), User.class);
            response.setResult(list);
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<Long> maxId() {
        Response<Long> response = new Response<>();
        try {
            response.setResult(userDaoExt.maxId());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<Date> minDate() {
        Response<Date> response = new Response<>();
        try {
            response.setResult(userDaoExt.minDate());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<List<User>> listTo(Long lastId, int limit) {
        Response<List<User>> response = new Response<>();
        try {
            response.setResult(BeanMapper.mapList(userDaoExt.listTo(lastId, limit), User.class));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<List<User>> listSince(Long lastId, String since, int limit) {
        Response<List<User>> response = new Response<>();
        try {
            response.setResult(BeanMapper.mapList(userDaoExt.listSince(lastId, since, limit), User.class));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<Paging<User>> paging(Integer offset, Integer limit) {
        Response<Paging<User>> response = new Response<>();
        try {
            response.setResult(this.getPaging(userDaoExt.paging(offset, limit)));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<Paging<User>> paging(Integer offset, Integer limit, User criteria) {
        Response<Paging<User>> response = new Response<>();
        try {
            response.setResult(this.getPaging(userDaoExt.paging(offset, limit, BeanMapper.convertObjectToMap(criteria))));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<Paging<User>> paging(Integer offset, Integer limit, Map<String, Object> criteria) {
        Response<Paging<User>> response = new Response<>();
        try {
            response.setResult(this.getPaging(userDaoExt.paging(offset, limit, criteria)));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public Response<Paging<User>> paging(Map<String, Object> criteria) {
        Response<Paging<User>> response = new Response<>();
        try {
            response.setResult(this.getPaging(userDaoExt.paging(criteria)));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }


    private User setUserType(User user){
        user.getRolesJson();
        user.setType(null);//TODO 陈增辉
        return user;
    }
    private Paging<User> getPaging(io.terminus.common.model.Paging<io.terminus.parana.user.model.User> page){
        Paging<User> paging = new Paging<>();
        paging.setTotal(page.getTotal());
        paging.setData(BeanMapper.mapList(page.getData(), User.class));
        return paging;
    }
}
