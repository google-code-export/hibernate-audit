/*==============================================================*/
/* DBMS name:      ORACLE Version 10g                           */
/* Created on:     12/9/2008 3:07:17 PM                         */
/*==============================================================*/

create sequence AUDIT_EVENT_PAIR_SEQ
increment by 50
start with 1;

create sequence AUDIT_EVENT_SEQ
increment by 50
start with 1;

create sequence AUDIT_LOGICAL_GROUP_ID_SEQ
start with 1
increment by 50;

create sequence AUDIT_TRANSACTION_SEQ
increment by 1
start with 1;

create sequence AUDIT_TYPE_FIELD_SEQ
increment by 50
start with 1;

create sequence AUDIT_TYPE_SEQ
increment by 50
start with 1;

/*==============================================================*/
/* Table: AUDIT_EVENT                                           */
/*==============================================================*/
create table AUDIT_EVENT  (
   AUDIT_EVENT_ID       NUMBER(30)                      not null,
   AUDIT_TRANSACTION_ID NUMBER(30)                      not null,
   AUDIT_TYPE_ID        NUMBER(30)                      not null,
   AUDIT_LOGICAL_GROUP_ID NUMBER(30),
   TARGET_ENTITY_ID     NUMBER(30)                      not null,
   EVENT_TYPE_NAME      varchar2(255),
   constraint PK_AUDIT_EVENT primary key (AUDIT_EVENT_ID)
);

/*==============================================================*/
/* Index: AUDIT_EVENT_IX2                                       */
/*==============================================================*/
create index AUDIT_EVENT_IX2 on AUDIT_EVENT (
   AUDIT_TRANSACTION_ID ASC
);

/*==============================================================*/
/* Index: AUDIT_EVENT_IX3                                       */
/*==============================================================*/
create index AUDIT_EVENT_IX3 on AUDIT_EVENT (
   AUDIT_TYPE_ID ASC
);

/*==============================================================*/
/* Table: AUDIT_EVENT_PAIR                                      */
/*==============================================================*/
create table AUDIT_EVENT_PAIR  (
   AUDIT_EVENT_PAIR_ID  NUMBER(30)                      not null,
   AUDIT_EVENT_ID       NUMBER(30)                      not null,
   AUDIT_TYPE_FIELD_ID  NUMBER(30)                      not null,
   IS_COLLECTION_IND    CHAR(1)                         not null,
   STRING_VALUE_TXT     varchar2(3000),
   constraint PK_AUDIT_EVENT_PAIR primary key (AUDIT_EVENT_PAIR_ID)
);

/*==============================================================*/
/* Index: AUDIT_EVENT_PAIR_IX2                                  */
/*==============================================================*/
create index AUDIT_EVENT_PAIR_IX2 on AUDIT_EVENT_PAIR (
   AUDIT_EVENT_ID ASC
);

/*==============================================================*/
/* Index: AUDIT_EVENT_PAIR_IX3                                  */
/*==============================================================*/
create index AUDIT_EVENT_PAIR_IX3 on AUDIT_EVENT_PAIR (
   AUDIT_TYPE_FIELD_ID ASC
);

/*==============================================================*/
/* Table: AUDIT_EVENT_PAIR_COLLECTION                           */
/*==============================================================*/
create table AUDIT_EVENT_PAIR_COLLECTION  (
   AUDIT_EVENT_PAIR_ID  NUMBER(30)                      not null,
   COLLECTION_ENTITY_ID number(30)                      not null
);

/*==============================================================*/
/* Index: AUDIT_EVENT_PAIR_COLLECTIO_IX1                        */
/*==============================================================*/
create index AUDIT_EVENT_PAIR_COLLECTIO_IX1 on AUDIT_EVENT_PAIR_COLLECTION (
   AUDIT_EVENT_PAIR_ID ASC
);

/*==============================================================*/
/* Table: AUDIT_LOGICAL_GROUP                                   */
/*==============================================================*/
create table AUDIT_LOGICAL_GROUP  (
   AUDIT_LOGICAL_GROUP_ID NUMBER(30)                      not null,
   AUDIT_TYPE_ID        NUMBER(30)                      not null,
   EXTERNAL_ID          NUMBER(30)                      not null,
   constraint PK_AUDIT_LOGICAL_GROUP primary key (AUDIT_LOGICAL_GROUP_ID)
);

/*==============================================================*/
/* Table: AUDIT_TRANSACTION                                     */
/*==============================================================*/
create table AUDIT_TRANSACTION  (
   AUDIT_TRANSACTION_ID NUMBER(30)                      not null,
   TRANSACTION_TMSTP    timestamp                       not null,
   TRANSACTION_USER_NAME varchar2(255),
   constraint PK_AUDIT_TRANSACTION primary key (AUDIT_TRANSACTION_ID)
);

/*==============================================================*/
/* Table: AUDIT_TYPE                                            */
/*==============================================================*/
create table AUDIT_TYPE  (
   AUDIT_TYPE_ID        NUMBER(30)                      not null,
   DTYPE_CODE           CHAR(1)                         not null,
   CLASS_NAME           varchar2(255)                   not null,
   LABEL_NAME           varchar2(255),
   ENTITY_ID_CLASS_NAME varchar2(255),
   COLLECTION_CLASS_NAME varchar2(255),
   constraint PK_AUDIT_TYPE primary key (AUDIT_TYPE_ID), unique (DTYPE_CODE, CLASS_NAME, ENTITY_ID_CLASS_NAME, COLLECTION_CLASS_NAME)
);

/*==============================================================*/
/* Table: AUDIT_TYPE_FIELD                                      */
/*==============================================================*/
create table AUDIT_TYPE_FIELD  (
   AUDIT_TYPE_FIELD_ID  NUMBER(30)                      not null,
   AUDIT_TYPE_ID        NUMBER(30)                      not null,
   LABEL_NAME           varchar2(255),
   FIELD_NAME           varchar2(255)                   not null,
   constraint PK_AUDIT_TYPE_FIELD primary key (AUDIT_TYPE_FIELD_ID), unique (AUDIT_TYPE_ID, FIELD_NAME)
);

/*==============================================================*/
/* Index: AUDIT_TYPE_FIELD_IX2                                  */
/*==============================================================*/
create index AUDIT_TYPE_FIELD_IX2 on AUDIT_TYPE_FIELD (
   AUDIT_TYPE_ID ASC
);

alter table AUDIT_EVENT
   add constraint FK_AUDIT_TYPE_EVENT foreign key (AUDIT_TYPE_ID)
      references AUDIT_TYPE (AUDIT_TYPE_ID);

alter table AUDIT_EVENT
   add constraint FK_AUDIT_TRANSACTION_EVENT foreign key (AUDIT_TRANSACTION_ID)
      references AUDIT_TRANSACTION (AUDIT_TRANSACTION_ID);

alter table AUDIT_EVENT
   add constraint FK_AUDIT_LOGICAL_GROUP_EVENT foreign key (AUDIT_LOGICAL_GROUP_ID)
      references AUDIT_LOGICAL_GROUP (AUDIT_LOGICAL_GROUP_ID);

alter table AUDIT_EVENT_PAIR
   add constraint FK_AUDIT_TYPE_FIELD_EVNT_PAIR foreign key (AUDIT_TYPE_FIELD_ID)
      references AUDIT_TYPE_FIELD (AUDIT_TYPE_FIELD_ID);

alter table AUDIT_EVENT_PAIR
   add constraint FK_AUDIT_EVENT_PAIR foreign key (AUDIT_EVENT_ID)
      references AUDIT_EVENT (AUDIT_EVENT_ID);

alter table AUDIT_EVENT_PAIR_COLLECTION
   add constraint FK_AUDIT_EVENT_PAIR_COLLECTION foreign key (AUDIT_EVENT_PAIR_ID)
      references AUDIT_EVENT_PAIR (AUDIT_EVENT_PAIR_ID);

alter table AUDIT_LOGICAL_GROUP
   add constraint FK_AUDIT_TYPE_LOGICAL_GRP foreign key (AUDIT_TYPE_ID)
      references AUDIT_TYPE (AUDIT_TYPE_ID);

alter table AUDIT_TYPE_FIELD
   add constraint FK_AUDIT_TYPE_FIELD foreign key (AUDIT_TYPE_ID)
      references AUDIT_TYPE (AUDIT_TYPE_ID);

