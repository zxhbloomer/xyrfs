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
 * security ????????????
 * @author zxh
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FsSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * ?????????????????????
     */
    @Autowired
    private FsAuthenticationSucessHandler fsAuthenticationSucessHandler;

    /**
     * ?????????????????????
     */
    @Autowired
    private FsAuthenticationFailureHandler fsAuthenticationFailureHandler;

    @Autowired
    private FsSecurityProperties fsSecurityProperties;

    @Qualifier("db1")
    @Autowired
    private DataSource dataSource;

    /**
     * ????????????
     * @return
     */
    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // Remove the ROLE_ prefix
        return new GrantedAuthorityDefaults("");
    }


    /**
     * spring security??????????????????????????????
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    /**
//     * ?????????????????????
//     * @param auth
//     * @throws Exception
//     */
//    @Autowired
//    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//            .withUser("admin").password("123456").roles("ADMIN");
//    }

    /**
     * ?????? rememberMe ??????????????????
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
            /** ????????????????????? */
            .accessDeniedHandler(accessDeniedHandler())
            .and()
                .addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class)
//                /** ????????????????????? */
//                .addFilterBefore(smsCodeFilter, UsernamePasswordAuthenticationFilter.class)
//                /** ????????????????????????????????? */
//                .addFilterBefore(imageCodeFilter, UsernamePasswordAuthenticationFilter.class)
                /** ???????????? */
                .formLogin()
                /** ??????????????? URL */
                .loginPage(fsSecurityProperties.getLoginUrl())
                .usernameParameter("username").passwordParameter("password")
                /** ?????????????????? URL */
                .loginProcessingUrl(fsSecurityProperties.getCode().getImage().getLoginProcessingUrl())
                /** ?????????????????? */
                .successHandler(fsAuthenticationSucessHandler)
                /** ?????????????????? */
                .failureHandler(fsAuthenticationFailureHandler)
            .and()
                /** ????????????????????? */
                .rememberMe()
                /** ?????? token ??????????????? */
                .tokenRepository(persistentTokenRepository())
                /** rememberMe ???????????????????????? */
                .tokenValiditySeconds(fsSecurityProperties.getRememberMeTimeout())
                /** ???????????????????????? */
//                .userDetailsService(userDetailService)
            .and()
                /** ?????? session????????? */
                .sessionManagement()
                /** ?????? session?????? */
                .invalidSessionStrategy(invalidSessionStrategy())
                /** ???????????????????????? */
                .maximumSessions(fsSecurityProperties.getMAX_SESSIONS())
                /** ??????????????????????????? */
                .expiredSessionStrategy(new FsExpiredSessionStrategy())
                /** ?????? session???????????? */
                .sessionRegistry(sessionRegistry())
            .and()
            .and()
                /** ???????????? */
                .logout()
                /** ????????????????????? */
                .addLogoutHandler(logoutHandler())
                /** ???????????? url */
                //                .logoutUrl(fsSecurityProperties.getLogoutUrl())
                /** ?????????????????? */
                //                .logoutSuccessUrl("/")
                /** ?????? JSESSIONID */
                .deleteCookies("JSESSIONID")
            .and()
                /** ???????????? */
                .authorizeRequests()
                /** ??????????????????????????? */
                .antMatchers(anonResourcesUrl).permitAll()
                .antMatchers(
                    /** ???????????? */
                    fsSecurityProperties.getLoginUrl(),
                    /** ???????????? url */
                    FsConstant.FEBS_REGIST_URL,
                    /** ??????????????????????????? */
                    fsSecurityProperties.getCode().getImage().getCreateUrl(),
                    /** ??????????????????????????? */
                    fsSecurityProperties.getCode().getSms().getCreateUrl()
                )
                /** ????????????????????? */
                .permitAll()
                /** ???????????? *//** ??????????????? */
                .anyRequest().authenticated()
            .and()
                .csrf().disable()
                /** ????????????????????????????????? */
//                .apply(fsSmsCodeAuthenticationSecurityConfig)
        ;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * ?????? javaconfig ?????????????????????????????? sessionRegistry
     * @return
     */
    @Bean
    public FsAuthenticationSucessHandler fsAuthenticationSucessHandler() {
        FsAuthenticationSucessHandler authenticationSucessHandler = new FsAuthenticationSucessHandler();
        authenticationSucessHandler.setSessionRegistry(sessionRegistry());
        return authenticationSucessHandler;
    }

    /**
     * ?????????????????????
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
