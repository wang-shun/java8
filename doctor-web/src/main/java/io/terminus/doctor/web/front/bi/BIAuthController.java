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

import java.util.HashMap;
import java.util.Map;
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

    /**
     * 生成签名
     *
     * @param pageName
     * @param params
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "{pageName}/sign")
    public String getUrlWithSign(@PathVariable String pageName, @RequestParam(required = false) Map<String, Object> params) {

        DoctorBiPages page = RespHelper.or500(doctorBiPagesReadService.findByName(pageName));
        if (null == page)
            throw new ServiceException("bi.page.not.found");
        if (StringUtils.isBlank(page.getUrl())) {
            throw new ServiceException("bi.page.url.blank");
        }
        if (StringUtils.isBlank(page.getToken()))
            throw new ServiceException("bi.page.token.blank");

        long timestamp = System.currentTimeMillis();
        TreeMap<String, Object> paramsWithOrder = new TreeMap<>(); //参数排序
        paramsWithOrder.put("_t", timestamp);
        if (params.containsKey("_where")) { //自定义参数
            paramsWithOrder.put("_where", params.get("_where"));
        }

        Map<String, Object> paramsInUrl = new HashMap<>();//URL中的参数
        int pIndex = page.getUrl().lastIndexOf("?");
        if (pIndex != -1) {
            Stream.of(page.getUrl().substring(pIndex + 1).split("&")).forEach(p -> {
                String[] d = p.split("=");
                if (d.length == 2) {
                    paramsInUrl.put(d[0], d[1]);
                }
            });
        }
        paramsInUrl.entrySet().stream()
                .filter(p -> p.getKey().equals("_project_key") || p.getKey().equals("_board_key") || p.getKey().equals("_role_key"))
                .forEach(p -> paramsWithOrder.put(p.getKey(), p.getValue()));

        String sign = Hashing.md5().newHasher().
                putString(Joiner.on('&').withKeyValueSeparator("=").join(paramsWithOrder), Charsets.UTF_8)
                .putString(page.getToken(), Charsets.UTF_8)
                .hash().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("&_t=").append(timestamp);
        if (params.containsKey("_where"))
            sb.append("&_where=").append(params.get("_where"));
        params.remove("_where");
        params.remove("_");
        params.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
        sb.append("&_sign=").append(sign);
        return page.getUrl() + sb.toString();

    }

}
