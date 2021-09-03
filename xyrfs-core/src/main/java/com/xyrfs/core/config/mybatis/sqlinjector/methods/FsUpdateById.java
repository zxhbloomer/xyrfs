package com.xyrfs.core.config.mybatis.sqlinjector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.methods.UpdateById;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;

/**
 * 重写方法，updatebyid
 * @author
 */
public class FsUpdateById extends UpdateById {

    /**
     * 重写方法，updatebyid
     * @param mapperClass
     * @param modelClass
     * @param tableInfo
     * @return
     */
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String method = "fsUpdateById";
        boolean logicDelete = tableInfo.isLogicDelete();
        SqlMethod sqlMethod = SqlMethod.UPDATE_BY_ID;
        final String additional = optlockVersion(tableInfo) + tableInfo.getLogicDeleteSql(true, false);
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
            sqlSet(logicDelete, false, tableInfo, false, ENTITY, ENTITY_DOT),
            tableInfo.getKeyColumn(), ENTITY_DOT + tableInfo.getKeyProperty(), additional);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addUpdateMappedStatement(mapperClass, modelClass, method, sqlSource);
    }
}
