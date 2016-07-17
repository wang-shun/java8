package io.terminus.doctor.basic.search.material;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.search.api.IndexExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 物料ElasticSearch搜索写接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
@RpcProvider
public class MaterialSearchWriteServiceImpl implements MaterialSearchWriteService {

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedMaterialFactory indexedMaterialFactory;

    @Autowired
    private IndexedMaterialTaskAction indexedMaterialTaskAction;

    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;

    @Override
    public Response<Boolean> index(Long materialId) {
        try {
            IndexedMaterial indexedBarn = indexedMaterialFactory.create(doctorBasicMaterialDao.findById(materialId));
            if (indexedBarn != null) {
                indexExecutor.submit(indexedMaterialTaskAction.indexTask(indexedBarn));
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("material indexed failed, material(id={}), cause by: {}",
                    materialId, Throwables.getStackTraceAsString(e));
            return Response.fail("material.index.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long materialId) {
        try {
            indexExecutor.submit(indexedMaterialTaskAction.deleteTask(materialId));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e) {
            log.error("material delete failed, material(id={}), cause by: {}",
                    materialId, Throwables.getStackTraceAsString(e));
            return Response.fail("material.delete.fail");
        }
    }

    @Override
    public Response<Boolean> update(Long materialId) {
        try {
            // 暂时不删除(只索引)
            return index(materialId);
        }catch (Exception e) {
            log.error("material update failed, material(id={}), cause by: {}",
                    materialId, Throwables.getStackTraceAsString(e));
            return Response.fail("material.update.fail");
        }
    }
}
