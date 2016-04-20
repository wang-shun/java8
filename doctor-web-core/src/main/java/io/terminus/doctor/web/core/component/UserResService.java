package io.terminus.doctor.web.core.component;

import com.google.common.collect.Lists;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.pampas.client.Export;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Effet
 */
@Service
public class UserResService {

    @Export(paramNames = {"loginUser"})
    public Response<List<String>> getAllowsOfLoginUser(BaseUser loginUser) {
        List<String> allows = Lists.newArrayList("");
        return Response.ok(allows);
    }
}
