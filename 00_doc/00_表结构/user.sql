drop table if exists m_user;

/*==============================================================*/
/* Table: m_user                                                */
/*==============================================================*/
create table m_user
(
   id                   bigint not null auto_increment,
   Type                 varchar(2) comment 'ϵͳ�û�=10,ְԱ=20,�ͻ�=30,��Ӧ��=40,����=50,��֤����Ա=60,��ƹ���Ա=70',
   Remark               text comment '����',
   Password             varchar(64) comment '����',
   IsDelete             tinyint comment '�Ƿ����Ѿ�ɾ��',
   IsLocked             tinyint comment '�Ƿ�����',
   Forbidden            tinyint comment '�Ƿ����',
   EectiveDate          datetime comment '��Чʱ��',
   InvalidationDate     datetime comment 'ʧЧʱ��',
   ErrCount             int comment '��¼�������',
   GroupID              bigint comment '�����û���',
   PWEectiveDate        DateTime comment '������Ч����',
   LockedTime           datetime comment '�û�����ʱ��',
   HomePhone            varchar(32) comment '��ͥ�绰',
   OicePhone            varchar(32) comment '�칫�ҵ绰',
   Tell                 varchar(32) comment '�ֻ�����',
   CreatorID            bigint comment '������',
   CreateTime           timestamp comment '����ʱ��',
   UpdateID             bigint comment '����޸���',
   UpdateTime           timestamp comment '����޸�ʱ��',
   primary key (id)
);
