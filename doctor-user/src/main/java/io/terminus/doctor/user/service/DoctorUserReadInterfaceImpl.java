package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Params;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.model.PagingDto;
import io.terminus.doctor.user.interfaces.model.RespDto;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.doctor.user.interfaces.service.DoctorUserReadInterface;
import io.terminus.parana.user.model.User;
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
@RpcProvider
public class DoctorUserReadInterfaceImpl implements DoctorUserReadInterface {

    private final UserDaoExt userDaoExt;

    @Autowired
    public DoctorUserReadInterfaceImpl(UserDaoExt userDaoExt){
        this.userDaoExt = userDaoExt;
    }

    @Override
    public RespDto<UserDto> findByNick(String nickname) {
        RespDto<UserDto> response = new RespDto<>();
        try {
            User paranaUser = userDaoExt.findByName(nickname);
            if (paranaUser == null) {
                return RespDto.ok(null);
            } else {
                response.setResult(BeanMapper.map(paranaUser, UserDto.class));
            }
        } catch(ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<UserDto> findByEmail(String email) {
        RespDto<UserDto> response = new RespDto<>();
        try {
            User paranaUser = userDaoExt.findByEmail(email);
            if (paranaUser == null) {
                return RespDto.ok(null);
            } else {
                response.setResult(BeanMapper.map(paranaUser, UserDto.class));
            }
        } catch(ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<UserDto> findByMobile(String mobile) {
        RespDto<UserDto> response = new RespDto<>();
        try {
            User paranaUser = userDaoExt.findByMobile(mobile);
            if (paranaUser == null) {
                return RespDto.ok(null);
            } else {
                response.setResult(BeanMapper.map(paranaUser, UserDto.class));
            }
        } catch(ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<UserDto> load(Integer id) {
        RespDto<UserDto> response = new RespDto<>();
        UserDto user = new UserDto();
        try {
            BeanMapper.copy(this.checkNotNull(userDaoExt.findById(id)), user);
            response.setResult(user);
        } catch(ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<UserDto> load(Long id) {
        RespDto<UserDto> response = new RespDto<>();
        UserDto user = new UserDto();
        try {
            BeanMapper.copy(this.checkNotNull(userDaoExt.findById(id)), user);
            response.setResult(user);
        } catch(ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<List<UserDto>> loads(List<Long> ids) {
        RespDto<List<UserDto>> response = new RespDto<>();
        try {
            List<UserDto> list = BeanMapper.mapList(userDaoExt.findByIds(ids), UserDto.class);
            response.setResult(list);
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<List<UserDto>> loads(Long id0, Long id1, Long... idn) {
        RespDto<List<UserDto>> response = new RespDto<>();
        try {
            List<UserDto> list = BeanMapper.mapList(userDaoExt.findByIds(id0, id1, idn), UserDto.class);
            response.setResult(list);
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<List<UserDto>> loadsBy(Map<String, Object> criteria) {
        RespDto<List<UserDto>> response = new RespDto<>();
        try {
            List<UserDto> list = BeanMapper.mapList(userDaoExt.list(criteria), UserDto.class);
            response.setResult(list);
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Long> maxId() {
        RespDto<Long> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.maxId());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<Date> minDate() {
        RespDto<Date> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.minDate());
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<List<UserDto>> listTo(Long lastId, int limit) {
        RespDto<List<UserDto>> response = new RespDto<>();
        try {
            response.setResult(BeanMapper.mapList(userDaoExt.listTo(lastId, limit), UserDto.class));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<List<UserDto>> listSince(Long lastId, String since, int limit) {
        RespDto<List<UserDto>> response = new RespDto<>();
        try {
            response.setResult(BeanMapper.mapList(userDaoExt.listSince(lastId, since, limit), UserDto.class));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<PagingDto<UserDto>> paging(Integer offset, Integer limit) {
        RespDto<PagingDto<UserDto>> response = new RespDto<>();
        try {
            response.setResult(this.getPagingDto(userDaoExt.paging(offset, limit)));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<PagingDto<UserDto>> paging(Integer offset, Integer limit, UserDto criteria) {
        RespDto<PagingDto<UserDto>> response = new RespDto<>();
        try {
            Map<String, Object> map = BeanMapper.convertObjectToMap(criteria);
            response.setResult(this.getPagingDto(userDaoExt.paging(offset, limit, map)));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<PagingDto<UserDto>> paging(Integer offset, Integer limit, Map<String, Object> criteria) {
        RespDto<PagingDto<UserDto>> response = new RespDto<>();
        try {
            response.setResult(this.getPagingDto(userDaoExt.paging(offset, limit, criteria)));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<PagingDto<UserDto>> paging(Map<String, Object> criteria) {
        RespDto<PagingDto<UserDto>> response = new RespDto<>();
        try {
            response.setResult(this.getPagingDto(userDaoExt.paging(criteria)));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<List<String>> listAllUserEmails(String userTypeName) {
        RespDto<List<String>> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.listAllUserEmails(userTypeName));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    @Override
    public RespDto<List<String>> listAllUserMobiles(String userTypeName) {
        RespDto<List<String>> response = new RespDto<>();
        try {
            response.setResult(userDaoExt.listAllUserMobiles(userTypeName));
        } catch (Exception e) {
            log.error("find user failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("find.user.failed");
        }
        return response;
    }

    private User checkNotNull(User user){
        if(user == null){
            throw new ServiceException("user.not.found");
        }
        return user;
    }
    
    private PagingDto<UserDto> getPagingDto(Paging<User> page){
        PagingDto<UserDto> paging = new PagingDto<>();
        paging.setTotal(page.getTotal());
        paging.setData(BeanMapper.mapList(page.getData(), UserDto.class));
        return paging;
    }
}
