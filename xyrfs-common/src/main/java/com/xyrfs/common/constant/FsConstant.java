package com.xyrfs.common.constant;

/**
 * @author zxh
 */
public class FsConstant {

    private FsConstant() {
    }

    // excel
    public static final String XLSX_SUFFIX = ".xlsx";
    public static final String XLS_SUFFIX = ".xls";
    public static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /** 图形验证码 session key */
    public static final String SESSION_KEY_IMAGE_CODE = "SESSION_KEY_IMAGE_CODE";
    /** 手机验证码 session key */
    public static final String SESSION_KEY_SMS_CODE = "SESSION_KEY_SMS_CODE";
    /** 用户注册 所保存的密码 */
    public static final String SESSION_KEY_USER_PASSWORD = "SESSION_KEY_USER_PASSWORD";

    // QQ 用户信息获取 URL
    public static final String GET_QQ_USER_INFO_URL = "https://graph.qq.com/user/get_user_info?oauth_consumer_key=%s&openid=%s";
    // QQ openId 获取 URL
    public static final String GET_QQ_OPEN_ID_URL = "https://graph.qq.com/oauth2.0/me?access_token=%s";
    // QQ 授权接口 URL
    public static final String QQ_AUTHORIZE_URL = "https://graph.qq.com/oauth2.0/authorize";
    // 获取 QQ AccessToken URL
    public static final String GET_QQ_ACCESSTOKEN_URL = "https://graph.qq.com/oauth2.0/token";
    // 微信用户信息获取 URL
    public static final String GET_WEIXIN_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?openid=";
    // 获取微信授权码 URL
    public static final String WEIXIN_AUTHORIZE_URL = "https://open.WeiXin.qq.com/connect/qrconnect";
    // 获取微信 AccessToken URL
    public static final String GET_WEIXIN_ACCESSTOKEN_URL = "https://api.WeiXin.qq.com/sns/oauth2/access_token";
    // 微信 Refresh Token URL
    public static final String WEIXIN_REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token";
    // 返回报文头 json格式，编码 utf-8
    public static final String JSON_UTF8 = "application/json;charset=utf-8";
    // 返回 html
    public static final String HTML_UTF8 = "text/html;charset=utf-8";
    // 用户注册 URL
    public static final String FEBS_REGIST_URL = "/user/regist";
    // 权限不足 URL
    public static final String FEBS_ACCESS_DENY_URL = "/access/deny/403";
    // 社交账号绑定成功 URL
    public static final String SOCIAL_BIND_SUCCESS_URL = "/social/bind/success";
    // 社交账号解绑成功 URL
    public static final String SOCIAL_UNBIND_SUCCESS_URL = "/social/unbind/success";
    // 社交账户 openId session key
    public static final String SESSION_KEY_SOCIAL_OPENID = "SESSION_KEY_SOCIAL_OPENID";

    // 第三方 API 接口地址
    public static final String MEIZU_WEATHER_URL = "http://aider.meizu.com/app/weather/listWeather";
    public static final String MRYW_TODAY_URL = "https://interface.meiriyiwen.com/article/today";
    public static final String MRYW_DAY_URL = "https://interface.meiriyiwen.com/article/day";
    public static final String TIME_MOVIE_HOT_URL = "https://api-m.mtime.cn/Showtime/LocationMovies.api";
    public static final String TIME_MOVIE_DETAIL_URL = "https://ticket-api-m.mtime.cn/movie/detail.api";
    public static final String TIME_MOVIE_COMING_URL = "https://api-m.mtime.cn/Movie/MovieComingNew.api";
    public static final String TIME_MOVIE_COMMENTS_URL = "https://ticket-api-m.mtime.cn/movie/hotComment.api";

    /**
     * 任务调度参数key
     */
    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";

    /**
     * redis前缀
     */
    public class SESSION_PREFIX {
        public static final String SESSION_USER_PREFIX_PREFIX = "XYRFS_USER_SESSION";
    }

    /**
     * redis前缀
     */
    public class REDIS_PREFIX {
        public static final String MQ_SEND_PREFIX = "XYRFS_MQ";// mq发送消息暂存到redis的prefix
        public static final String MQ_CONSUME_FAILT_PREFIX = "XYRFS_MQ_CONSUME_FAILT_PREFIX";
        public static final String MQ_CONSUME_RETURN_PREFIX = "XYRFS_MQ_CONSUME_RETURN_PREFIX";

        /** 菜单权限、操作权限 */
//        public static final String PERMISSION_MENU_OPERATION_PREFIX = "PERMISSION_MENU_OPERATION_PREFIX";
    }

    /**
     * 缓存使用常量
     */
    public class CACHE_PC {
        /** 地区级联 */
        public static final String CACHE_AREAS_CASCADER = "CACHE_AREAS_CASCADER";
        /** 字典表 */
        public static final String CACHE_DICT_TYPE = "CACHE_DICT_TYPE";
        /** 列宽 */
        public static final String CACHE_COLUMNS_TYPE = "CACHE_COLUMNS_TYPE";
        /** 系统icon */
        public static final String CACHE_SYSTEM_ICON_TYPE = "CACHE_SYSTEM_ICON_TYPE";
    }

    /**
     * 短信验证码类型
     */
    public class SMS_CODE_TYPE {
        /** 未知 */
        public static final String SMS_CODE_TYPE_NO_TYPE = "0";
        /** 注册 */
        public static final String SMS_CODE_TYPE_REGIST = "1";
        /** 忘记密码 */
        public static final String SMS_CODE_TYPE_FORGET = "2";
        /** 修改绑定手机 */
        public static final String SMS_CODE_TYPE_CHANGE_MOBILE = "3";
    }

    /**
     * 注册时
     */
    public class SIGN_UP {
        public static final String TENANT_SERIAL_NO = "0001";
    }

    /**
     * 日志分类
     */
    public class LOG_FLG {
        public static final String OK = "OK";
        public static final String NG = "NG";
    }

    /**
     * 刷新session时使用loginuserid 还是 staffid
     */
    public class LOGINUSER_OR_STAFF_ID {
        public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
        public static final String STAFF_ID = "STAFF_ID";
    }

    /**
     *  平台类型
     */
    public class PLATFORM {
        public static final int OTHER = 0;
        public static final int PC = 1;
        public static final int APP = 2;
    }

    /**
     * 操作日志内容
     */
    public class OPERATION {

        /**
         * 集团
         */
        public class M_GROUP {
            public static final String TABLE_NAME = "m_group";

            public static final String OPER_INSERT = "集团主表新增";
            public static final String OPER_UPDATE = "集团主表更新";
            public static final String OPER_LOGIC_DELETE = "集团主表逻辑删除";
        }

        /**
         * 组织机构
         */
        public class M_ORG {
            public static final String TABLE_NAME = "m_org";

            public static final String OPER_INSERT = "组织主表新增";
            public static final String OPER_UPDATE = "组织主表更新";
            public static final String OPER_DELETE = "组织主表物理删除";
            public static final String OPER_DRAG_DROP = "组织主表拖拽操作";
            public static final String OPER_POSITION_STAFF = "用户组织机构关系表，成员维护";
        }

        /**
         * 用户组织机构关系表
         */
        public class M_STAFF_ORG {
            public static final String TABLE_NAME = "m_staff_org";

            public static final String OPER_INSERT = "用户组织机构关系表新增";
            public static final String OPER_UPDATE = "用户组织机构关系表更新";
            public static final String OPER_DELETE = "用户组织机构关系表物理删除";
            public static final String OPER_POSITION_STAFF = "用户组织机构关系表，成员维护";
        }
    }

    /**
     * 顶部导航栏类型
     */
    public class TOP_NAV {
        /**
         * 按路径查询
         */
        public static final String TOP_NAV_FIND_BY_PATH = "find_by_path";
        /**
         * 按排序查询
         */
        public static final String TOP_NAV_FIND_BY_INDEX = "find_by_index";
    }
}
