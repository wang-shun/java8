package io.terminus.doctor.web.front.bi;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.model.DoctorBiPages;
import io.terminus.doctor.basic.service.DoctorBiPagesReadService;
import io.terminus.doctor.common.utils.RespHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2018/1/5.
 */
@RestController
@RequestMapping("/api/doctor/bi/")
public class BIAuthController {

    @RpcConsumer
    private DoctorBiPagesReadService doctorBiPagesReadService;

    @RequestMapping(method = RequestMethod.GET, value = "{pageName}/sign")
    public String getUrlWithSign(@PathVariable String pageName, @RequestParam(required = false) String params) {

        DoctorBiPages page = RespHelper.or500(doctorBiPagesReadService.findByName(pageName));
        if (null == page)
            throw new ServiceException("bi.page.not.found");

        long timestamp = System.currentTimeMillis();
        TreeMap<String, Object> paramsWithOrder = new TreeMap<>();
        paramsWithOrder.put("_t", timestamp);
        if (StringUtils.isNotBlank(params)) {
            paramsWithOrder.put("_where", params);
        }
        if (StringUtils.isBlank(page.getUrl())) {
            throw new ServiceException("bi.page.url.blank");
        }

        int pIndex = page.getUrl().lastIndexOf("?");
        if (pIndex != -1) {
            Stream.of(page.getUrl().substring(pIndex + 1).split("&")).forEach(p -> {
                String[] d = p.split("=");
                if (d.length == 2) {
                    paramsWithOrder.put(d[0], d[1]);
                }
            });
        }

        String sign = Hashing.md5().newHasher().
                putString(Joiner.on('&').withKeyValueSeparator("=").join(paramsWithOrder), Charsets.UTF_8)
                .putString("", Charsets.UTF_8)
                .hash().toString();
        return page.getUrl() + "&_t=" + timestamp + (StringUtils.isNotBlank(params) ? "&_where=" + params : "") + "&sign=" + sign;

    }

}
