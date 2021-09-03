package com.xyrfs.core.config.mybatis.sqlinjector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.xyrfs.core.config.mybatis.sqlinjector.methods.FsUpdateById;

import java.util.List;

/**
 * 自定义Sql注入
 */
public class FsSqlInjector extends DefaultSqlInjector {
    /**
     * 如果只需增加方法，保留MP自带方法
     * 可以super.getMethodList() 再add
     * @return
     */
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        //增加自定义方法
        methodList.add(new FsUpdateById());
        return methodList;
    }
}
