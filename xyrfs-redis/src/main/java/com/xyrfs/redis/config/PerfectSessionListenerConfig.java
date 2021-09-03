//package com.xyrfs.redis.config;
//
//import com.xyrfs.redis.listener.SpringHttpSessionListener;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
//
//import javax.servlet.http.HttpSessionListener;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// */
//@Deprecated
//public class FsSessionListenerConfig extends RedisHttpSessionConfiguration {
//
//    public FsSessionListenerConfig() {
//        List<HttpSessionListener> list = new ArrayList<>();
//        list.add(new SpringHttpSessionListener());
//        this.setHttpSessionListeners(list);
//        // session 过期时间30分钟
//        this.setMaxInactiveIntervalInSeconds(30 * 60);
//    }
//
//    /**
//     * 添加session 监听
//     * @param listeners
//     */
//    @Override
//    public void setHttpSessionListeners(List<HttpSessionListener> listeners) {
//        super.setHttpSessionListeners(listeners);
//    }
//
//    /**
//     * 设置session过期时间
//     * @param maxInactiveIntervalInSeconds
//     */
//    @Override
//    public void setMaxInactiveIntervalInSeconds(int maxInactiveIntervalInSeconds) {
//        super.setMaxInactiveIntervalInSeconds(maxInactiveIntervalInSeconds);
//    }
//}