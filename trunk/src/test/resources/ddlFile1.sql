/* comment */

create table A  (
   ID       NUMBER(30)                      not null,
   constraint PK_A primary key (ID)
);
create table B  (
   ID       NUMBER(30)                      not null,
   constraint PK_A primary key (ID)
);

/* comment 2 */

create index B_IX on A (
   ID ASC
);

/* comment 3 */

alter table A
   add constraint FK_A foreign key (SOMETHING)
      references SOMETHING_ELSE (SOMETHING_ELSE_ID);

create sequence A_SEQ;

