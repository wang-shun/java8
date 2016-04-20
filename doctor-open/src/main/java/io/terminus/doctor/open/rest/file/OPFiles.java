package io.terminus.doctor.open.rest.file;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import io.terminus.doctor.web.core.image.FileHelper;
import io.terminus.doctor.web.core.image.UploadDto;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.pampas.openplatform.entity.OPUploadFile;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-04-05 3:11 PM  <br>
 * Author: xiao
 */
@Slf4j
@OpenBean
public class OPFiles {
    @Autowired
    private FileHelper fileHelper;

    @OpenMethod(key="file.upload", paramNames = {"file"})
    public UploadDto upload(MultipartFile file) {
        log.debug("enter file upload");
        // unbind user device
        log.debug("file type {}", file.getContentType());
        log.debug("file length {}", file.getSize());
        log.debug("file name {}", file.getOriginalFilename());

        try {
            return fileHelper.upload(UserUtil.getUserId(), file);
        } catch (Exception e) {
            log.error("fail to upload file {}, cause: \n {}", file, Throwables.getStackTraceAsString(e));
            throw new OPClientException("file.upload.fail");
        }
    }

    @OpenMethod(key="multiple.file.upload", paramNames = {"files"})
    public Map<String, List<UploadDto>> bulkUpload(OPUploadFile opUploadFile) {
        log.debug("enter file upload");

        Map<String, List<UploadDto>> mappedUploads = Maps.newLinkedHashMap();


        for (String formName : opUploadFile.getMappedMultipartFiles().keySet()) {
            log.debug("formName name:{}", formName);
            log.debug("form {} size: {}", formName, opUploadFile.getMappedMultipartFiles().get(formName).size());

            int count = 0;
            List<UploadDto> uploadDtos = Lists.newArrayList();

            for (MultipartFile file : opUploadFile.getMappedMultipartFiles().get(formName)) {
                count ++;
                log.debug("file {} type {}", count, file.getContentType());
                log.debug("file {} length {}", count, file.getSize());
                log.debug("file {} name {}", count, file.getOriginalFilename());
                uploadDtos.add(fileHelper.upload(UserUtil.getUserId(), file));
            }

            mappedUploads.put(formName, uploadDtos);
        }
        return mappedUploads;
    }
}
