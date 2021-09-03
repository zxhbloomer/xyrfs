drop table if exists m_user;

/*==============================================================*/
/* Table: m_user                                                */
/*==============================================================*/
create table m_user
(
   id                   bigint not null auto_increment,
   Type                 varchar(2) comment '系统用户=10,职员=20,客户=30,供应商=40,其他=50,认证管理员=60,审计管理员=70',
   Remark               text comment '描述',
   Password             varchar(64) comment '密码',
   IsDelete             tinyint comment '是否是已经删除',
   IsLocked             tinyint comment '是否锁定',
   Forbidden            tinyint comment '是否禁用',
   EectiveDate          datetime comment '生效时间',
   InvalidationDate     datetime comment '失效时间',
   ErrCount             int comment '登录错误次数',
   GroupID              bigint comment '所属用户组',
   PWEectiveDate        DateTime comment '密码生效日期',
   LockedTime           datetime comment '用户锁定时间',
   HomePhone            varchar(32) comment '家庭电话',
   OicePhone            varchar(32) comment '办公室电话',
   Tell                 varchar(32) comment '手机号码',
   CreatorID            bigint comment '创建者',
   CreateTime           timestamp comment '创建时间',
   UpdateID             bigint comment '最后修改者',
   UpdateTime           timestamp comment '最后修改时间',
   primary key (id)
);
