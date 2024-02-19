-- Sprint 9 change for Super Admin
insert into role (ID,NAME,PRESENT_STATUS) values (99,'SUPER ADMIN',1);
alter table ORGANIZATION add column COMPANY_ID int(32) default null;