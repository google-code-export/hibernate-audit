/*==============================================================*/
/* DDL code for HBA 1.0.0.Alpha6                                */
/*==============================================================*/

/*==============================================================*/
/* Table: AUDIT_CLASS                                           */
/*==============================================================*/
create table AUDIT_CLASS  (
   AUDIT_CLASS_ID       NUMBER(30)                      not null,
   DTYPE                CHAR(1)                         not null,
   CLASS_NAME           varchar2(255),
   LABEL                varchar2(255),
   ENTITY_ID_CLASS_NAME varchar2(255),
   COLLECTION_CLASS_NAME varchar2(255),
   constraint PK_AUDIT_CLASS primary key (AUDIT_CLASS_ID)
);

/*==============================================================*/
/* Table: AUDIT_CLASS_FIELD                                     */
/*==============================================================*/
create table AUDIT_CLASS_FIELD  (
   AUDIT_CLASS_FIELD_ID NUMBER(30)                      not null,
   AUDIT_CLASS_ID       NUMBER(30)                      not null,
   LABEL                varchar2(255),
   FIELD_NAME           varchar2(255),
   constraint PK_AUDIT_CLASS_FIELD primary key (AUDIT_CLASS_FIELD_ID)
);

/*==============================================================*/
/* Index: AUDIT_CLASS_FIELD_IX2                                 */
/*==============================================================*/
create index AUDIT_CLASS_FIELD_IX2 on AUDIT_CLASS_FIELD (
   AUDIT_CLASS_ID ASC
);

/*==============================================================*/
/* Table: AUDIT_EVENT                                           */
/*==============================================================*/
create table AUDIT_EVENT  (
   AUDIT_EVENT_ID       NUMBER(30)                      not null,
   AUDIT_TRANSACTION_ID NUMBER(30)                      not null,
   AUDIT_CLASS_ID       NUMBER(30)                      not null,
   TARGET_ENTITY_ID     NUMBER(30),
   EVENT_TYPE           varchar2(255),
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
   AUDIT_CLASS_ID ASC
);

/*==============================================================*/
/* Table: AUDIT_EVENT_PAIR                                      */
/*==============================================================*/
create table AUDIT_EVENT_PAIR  (
   AUDIT_EVENT_PAIR_ID  NUMBER(30)                      not null,
   AUDIT_EVENT_ID       NUMBER(30)                      not null,
   AUDIT_CLASS_FIELD_ID NUMBER(30)                      not null,
   IS_COLLECTION        CHAR(1)                         not null,
   STRING_VALUE         varchar2(3000),
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
   AUDIT_CLASS_FIELD_ID ASC
);

/*==============================================================*/
/* Table: AUDIT_EVENT_PAIR_COLLECTION                           */
/*==============================================================*/
create table AUDIT_EVENT_PAIR_COLLECTION  (
   AUDIT_EVENT_PAIR_ID  NUMBER(30)                      not null,
   COLLECTION_ENTITY_ID number(19,0)                    not null
);

/*==============================================================*/
/* Index: AUDIT_EVENT_PAIR_COLLECTIO_IX1                        */
/*==============================================================*/
create index AUDIT_EVENT_PAIR_COLLECTIO_IX1 on AUDIT_EVENT_PAIR_COLLECTION (
   AUDIT_EVENT_PAIR_ID ASC
);

/*==============================================================*/
/* Table: AUDIT_TRANSACTION                                     */
/*==============================================================*/
create table AUDIT_TRANSACTION  (
   AUDIT_TRANSACTION_ID NUMBER(30)                      not null,
   TRANSACTION_TMSTP    timestamp,
   TRANSACTION_USER     varchar2(255),
   constraint PK_AUDIT_TRANSACTION primary key (AUDIT_TRANSACTION_ID)
);

alter table AUDIT_CLASS_FIELD
   add constraint FK_AUDIT_CLASS_FIELD foreign key (AUDIT_CLASS_ID)
      references AUDIT_CLASS (AUDIT_CLASS_ID);

alter table AUDIT_EVENT
   add constraint FK_AUDIT_CLASS_EVENT foreign key (AUDIT_CLASS_ID)
      references AUDIT_CLASS (AUDIT_CLASS_ID);

alter table AUDIT_EVENT
   add constraint FK_AUDIT_TRANSACTION_EVENT foreign key (AUDIT_TRANSACTION_ID)
      references AUDIT_TRANSACTION (AUDIT_TRANSACTION_ID);

alter table AUDIT_EVENT_PAIR
   add constraint FK_AUDIT_CLASS_FIELD_EVNT_PAIR foreign key (AUDIT_CLASS_FIELD_ID)
      references AUDIT_CLASS_FIELD (AUDIT_CLASS_FIELD_ID);

alter table AUDIT_EVENT_PAIR
   add constraint FK_AUDIT_EVENT_PAIR foreign key (AUDIT_EVENT_ID)
      references AUDIT_EVENT (AUDIT_EVENT_ID);

alter table AUDIT_EVENT_PAIR_COLLECTION
   add constraint FK_AUDIT_EVENT_PAIR_COLLECTION foreign key (AUDIT_EVENT_PAIR_ID)
      references AUDIT_EVENT_PAIR (AUDIT_EVENT_PAIR_ID);

create sequence AUDIT_CLASS_FIELD_SEQ;

create sequence AUDIT_CLASS_SEQ;

create sequence AUDIT_EVENT_PAIR_SEQ;

create sequence AUDIT_EVENT_SEQ;

create sequence AUDIT_TRANSACTION_SEQ;
