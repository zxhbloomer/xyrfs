package com.xyrfs.security.config;

import com.xyrfs.common.constant.FsConstant;
import com.xyrfs.security.code.ValidateCodeGenerator;
import com.xyrfs.security.cors.CorsFilter;
import com.xyrfs.security.handler.FsAuthenticationAccessDeniedHandler;
import com.xyrfs.security.handler.FsAuthenticationFailureHandler;
import com.xyrfs.security.handler.FsAuthenticationSucessHandler;
import com.xyrfs.security.handler.FsLogoutHandler;
import com.xyrfs.security.properties.FsSecurityProperties;
import com.xyrfs.security.session.FsExpiredSessionStrategy;
import com.xyrfs.security.session.FsInvalidSessionStrategy;
import com.xyrfs.security.xss.XssFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * security 配置中心
 * @author zxh
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FsSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 登录成功处理器
     */
    @Autowired
    private FsAuthenticationSucessHandler fsAuthenticationSucessHandler;

    /**
     * 登录失败处理器
     */
    @Autowired
    private FsAuthenticationFailureHandler fsAuthenticationFailureHandler;

    @Autowired
    private FsSecurityProperties fsSecurityProperties;

    @Qualifier("db1")
    @Autowired
    private DataSource dataSource;

    /**
     * 权限前缀
     * @return
     */
    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // Remove the ROLE_ prefix
        return new GrantedAuthorityDefaults("");
    }


    /**
     * spring security自带的密码加密工具类
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    /**
//     * 默认的登录用户
//     * @param auth
//     * @throws Exception
//     */
//    @Autowired
//    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//            .withUser("admin").password("123456").roles("ADMIN");
//    }

    /**
     * 处理 rememberMe 自动登录认证
     * @return
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String[] anonResourcesUrl = StringUtils.splitByWholeSeparatorPreserveAllTokens(
            fsSecurityProperties.getAnonResourcesUrl(),",");

        http.exceptionHandling()
            /** 权限不足处理器 */
            .accessDeniedHandler(accessDeniedHandler())
            .and()
                .addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class)
//                /** 短信验证码校验 */
//                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class)
//                /** 添加图形证码校验过滤器 */
//                .addFilterBefore(imageCodeFilter, UsernamePasswordAuthenticationFilter.class)
                /** 表单方式 */
                .formLogin()
                /** 未认证跳转 URL */
                .loginPage(fsSecurityProperties.getLoginUrl())
                .usernameParameter("username").passwordParameter("password")
                /** 处理登录认证 URL */
                .loginProcessingUrl(fsSecurityProperties.getCode().getImage().getLoginProcessingUrl())
                /** 处理登录成功 */
                .successHandler(fsAuthenticationSucessHandler)
                /** 处理登录失败 */
                .failureHandler(fsAuthenticationFailureHandler)
            .and()
                /** 添加记住我功能 */
                .rememberMe()
                /** 配置 token 持久化仓库 */
                .tokenRepository(persistentTokenRepository())
                /** rememberMe 过期时间，单为秒 */
                .tokenValiditySeconds(fsSecurityProperties.getRememberMeTimeout())
                /** 处理自动登录逻辑 */
//                .userDetailsService(userDetailService)
            .and()
                /** 配置 session管理器 */
                .sessionManagement()
                /** 处理 session失效 */
                .invalidSessionStrategy(invalidSessionStrategy())
                /** 最大并发登录数量 */
                .maximumSessions(fsSecurityProperties.getMAX_SESSIONS())
                /** 处理并发登录被踢出 */
                .expiredSessionStrategy(new FsExpiredSessionStrategy())
                /** 配置 session注册中心 */
                .sessionRegistry(sessionRegistry())
            .and()
            .and()
                /** 配置登出 */
                .logout()
                /** 配置登出处理器 */
                .addLogoutHandler(logoutHandler())
                /** 处理登出 url */
                //                .logoutUrl(fsSecurityProperties.getLogoutUrl())
                /** 登出后跳转到 */
                //                .logoutSuccessUrl("/")
                /** 删除 JSESSIONID */
                .deleteCookies("JSESSIONID")
            .and()
                /** 授权配置 */
                .authorizeRequests()
                /** 免认证静态资源路径 */
                .antMatchers(anonResourcesUrl).permitAll()
                .antMatchers(
                    /** 登录路径 */
                    fsSecurityProperties.getLoginUrl(),
                    /** 用户注册 url */
                    FsConstant.FEBS_REGIST_URL,
                    /** 创建图片验证码路径 */
                    fsSecurityProperties.getCode().getImage().getCreateUrl(),
                    /** 创建短信验证码路径 */
                    fsSecurityProperties.getCode().getSms().getCreateUrl()
                )
                /** 配置免认证路径 */
                .permitAll()
                /** 所有请求 *//** 都需要认证 */
                .anyRequest().authenticated()
            .and()
                .csrf().disable()
                /** 添加短信验证码认证流程 */
//                .apply(fsSmsCodeAuthenticationSecurityConfig)
        ;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 使用 javaconfig 的方式配置是为了注入 sessionRegistry
     * @return
     */
    @Bean
    public FsAuthenticationSucessHandler fsAuthenticationSucessHandler() {
        FsAuthenticationSucessHandler authenticationSucessHandler = new FsAuthenticationSucessHandler();
        authenticationSucessHandler.setSessionRegistry(sessionRegistry());
        return authenticationSucessHandler;
    }

    /**
     * 配置登出处理器
     * @return
     */
    @Bean
    public LogoutHandler logoutHandler(){
        FsLogoutHandler fsLogoutHandler = new FsLogoutHandler();
        fsLogoutHandler.setSessionRegistry(sessionRegistry());
        return fsLogoutHandler;
    }

    @Bean
    public InvalidSessionStrategy invalidSessionStrategy(){
        FsInvalidSessionStrategy fsInvalidSessionStrategy = new FsInvalidSessionStrategy();
        fsInvalidSessionStrategy.setFsSecurityProperties(fsSecurityProperties);
        return fsInvalidSessionStrategy;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new FsAuthenticationAccessDeniedHandler();
    }

    /**
     * XssFilter Bean
     */
    @Bean
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public FilterRegistrationBean xssFilterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new XssFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("excludes", "/favicon.ico,/img/*,/js/*,/css/*");
        initParameters.put("isIncludeRichText", "true");
        filterRegistrationBean.setInitParameters(initParameters);
        return filterRegistrationBean;
    }

    @Override
    public void configure(WebSecurity webSecurity) {
        String[] anonResourcesUrl = StringUtils.splitByWholeSeparatorPreserveAllTokens(
            fsSecurityProperties.getAnonResourcesUrl(),",");
        webSecurity.ignoring()
            .antMatchers(anonResourcesUrl);
    }

}
