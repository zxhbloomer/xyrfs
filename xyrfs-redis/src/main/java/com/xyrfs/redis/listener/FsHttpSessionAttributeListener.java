package com.xyrfs.redis.listener;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * 发现在spring boot 中HttpSessionAttributeListener，不能被调用
 * https://github.com/spring-projects/spring-session/issues/5
 */
@Slf4j
@WebListener
public class FsHttpSessionAttributeListener implements HttpSessionAttributeListener {

    @Override
    public void attributeAdded(HttpSessionBindingEvent se) {
        log.debug("Attribute 添加内容");
        log.debug("Attribute Name:" + se.getName());
        log.debug("Attribute Value:" + se.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent se) {
        log.debug("attribute 删除:" + se.getName());
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent se) {
        log.debug("Attribute 替换内容");
        log.debug("Attribute Name:" + se.getName());
        log.debug("Attribute Old Value:" + se.getValue());
    }
}
