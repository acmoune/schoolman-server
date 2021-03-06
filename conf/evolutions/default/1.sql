-- Initial DB creation

-- !Ups

create table "DEPARTMENT" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"title" VARCHAR NOT NULL UNIQUE);
create unique index "dep_tit_idx" on "DEPARTMENT" ("title");
create table "PROGRAM" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"department" BIGINT NOT NULL,"title" VARCHAR NOT NULL UNIQUE,"description" VARCHAR NOT NULL,"logo" VARCHAR,"link" VARCHAR);
create index "pro_dep_idx" on "PROGRAM" ("department");
create unique index "pro_tit_idx" on "PROGRAM" ("title");
create table "PLAN" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"program" BIGINT NOT NULL,"title" VARCHAR NOT NULL UNIQUE,"description" VARCHAR NOT NULL);
create index "pla_pro_idx" on "PLAN" ("program");
create unique index "pla_tit_idx" on "PLAN" ("title");
create table "TRAINING" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"program" BIGINT NOT NULL,"title" VARCHAR NOT NULL UNIQUE,"description" VARCHAR NOT NULL,"required_optional_units" INTEGER NOT NULL,"prerequisites" VARCHAR,"qualifications" VARCHAR,"link" VARCHAR,"banner" VARCHAR);
create index "tra_pro_idx" on "TRAINING" ("program");
create unique index "tra_tit_idx" on "TRAINING" ("title");
create table "TRAINING_PLAN" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"training" BIGINT NOT NULL,"plan" BIGINT NOT NULL,"duration" VARCHAR NOT NULL);
create index "tp_pla_idx" on "TRAINING_PLAN" ("plan");
create unique index "tp_tra_pla_idx" on "TRAINING_PLAN" ("training","plan");
create table "TRAINING_UNIT" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"training" BIGINT NOT NULL,"title" VARCHAR NOT NULL UNIQUE,"syllabus" VARCHAR NOT NULL,"optional" BOOLEAN NOT NULL);
create unique index "uni_tit_idx" on "TRAINING_UNIT" ("title");
create index "uni_tra_idx" on "TRAINING_UNIT" ("training");
create table "TRAINING_SESSION" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"training_plan" BIGINT NOT NULL,"title" VARCHAR NOT NULL UNIQUE,"startDate" DATE,"duration_unit" VARCHAR NOT NULL,"duration" DOUBLE PRECISION NOT NULL,"status" VARCHAR NOT NULL);
create unique index "ses_tit_idx" on "TRAINING_SESSION" ("title");
create index "ses_tp_idx" on "TRAINING_SESSION" ("training_plan");
create table "ACCOUNT" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"email" VARCHAR NOT NULL UNIQUE,"password" VARCHAR NOT NULL,"role" VARCHAR NOT NULL,"locked" BOOLEAN NOT NULL,"full_name" VARCHAR NOT NULL);
create unique index "acc_ema_idx" on "ACCOUNT" ("email");
create table "TRAINING_FEE" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"training_plan" BIGINT NOT NULL,"description" VARCHAR NOT NULL,"fee_type" VARCHAR NOT NULL,"amount" DOUBLE PRECISION NOT NULL,"promotional_amount" DOUBLE PRECISION,"optional" BOOLEAN NOT NULL);
create index "tf_tp_idx" on "TRAINING_FEE" ("training_plan");
create table "TRAINING_SESSION_FEE" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"training_session" BIGINT NOT NULL,"description" VARCHAR NOT NULL,"fee_type" VARCHAR NOT NULL,"amount" DOUBLE PRECISION NOT NULL,"promotional_amount" DOUBLE PRECISION,"optional" BOOLEAN NOT NULL);
create index "sf_ses_idx" on "TRAINING_SESSION_FEE" ("training_session");
create table "ENROLLMENT" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"account" BIGINT NOT NULL,"training_session" BIGINT NOT NULL,"status" VARCHAR NOT NULL,"notes" VARCHAR);
create index "enr_acc_idx" on "ENROLLMENT" ("account");
create unique index "enr_acc_ses_idx" on "ENROLLMENT" ("training_session","account");
create index "enr_ses_idx" on "ENROLLMENT" ("training_session");
create table "BILL" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"training_session_fee" BIGINT NOT NULL,"enrollment_id" BIGINT NOT NULL,"amount" DOUBLE PRECISION NOT NULL,"status" VARCHAR NOT NULL,"deadline" DATE);
create index "bil_enr_idx" on "BILL" ("enrollment_id");
create unique index "bil_enr_ses_idx" on "BILL" ("enrollment_id","training_session_fee");
create table "PAYMENT" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"bill" BIGINT NOT NULL,"amount" DOUBLE PRECISION NOT NULL,"date" DATE NOT NULL);
create index "pay_bil_idx" on "PAYMENT" ("bill");
create table "ACCOUNT_PROFILE" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"account" BIGINT NOT NULL UNIQUE,"birth_date" DATE NOT NULL,"birth_place" VARCHAR NOT NULL,"residence" VARCHAR NOT NULL,"phone_number" VARCHAR NOT NULL,"nationality" VARCHAR NOT NULL,"nic_number" VARCHAR,"nic_date_of_issue" DATE,"nic_place_of_issue" VARCHAR,"highest_academic_qualification" VARCHAR,"year_of_issue" INTEGER,"english_grade_in_gceol" VARCHAR,"mathematics_grade_in_gceol" VARCHAR,"professional_qualification" VARCHAR,"employment_status" VARCHAR NOT NULL,"job_title" VARCHAR,"years_of_experience" INTEGER,"employer_name" VARCHAR,"employer_address" VARCHAR,"other_details" VARCHAR);
create unique index "prof_acc_idx" on "ACCOUNT_PROFILE" ("account");
alter table "PROGRAM" add constraint "pro_dep_fk" foreign key("department") references "DEPARTMENT"("id") on update NO ACTION on delete NO ACTION;
alter table "PLAN" add constraint "pla_pro_fk" foreign key("program") references "PROGRAM"("id") on update NO ACTION on delete NO ACTION;
alter table "TRAINING" add constraint "tra_pro_fk" foreign key("program") references "PROGRAM"("id") on update NO ACTION on delete NO ACTION;
alter table "TRAINING_PLAN" add constraint "tp_pla_fk" foreign key("plan") references "PLAN"("id") on update NO ACTION on delete NO ACTION;
alter table "TRAINING_PLAN" add constraint "tp_tra_fk" foreign key("training") references "TRAINING"("id") on update NO ACTION on delete NO ACTION;
alter table "TRAINING_UNIT" add constraint "uni_tra_fk" foreign key("training") references "TRAINING"("id") on update NO ACTION on delete NO ACTION;
alter table "TRAINING_SESSION" add constraint "ses_tp_fk" foreign key("training_plan") references "TRAINING_PLAN"("id") on update NO ACTION on delete NO ACTION;
alter table "TRAINING_FEE" add constraint "tf_tp_fk" foreign key("training_plan") references "TRAINING_PLAN"("id") on update NO ACTION on delete NO ACTION;
alter table "TRAINING_SESSION_FEE" add constraint "sf_ses_fk" foreign key("training_session") references "TRAINING_SESSION"("id") on update NO ACTION on delete NO ACTION;
alter table "ENROLLMENT" add constraint "enr_acc_fk" foreign key("account") references "ACCOUNT"("id") on update NO ACTION on delete NO ACTION;
alter table "ENROLLMENT" add constraint "enr_ses_fk" foreign key("training_session") references "TRAINING_SESSION"("id") on update NO ACTION on delete NO ACTION;
alter table "BILL" add constraint "bil_enr_fk" foreign key("enrollment_id") references "ENROLLMENT"("id") on update NO ACTION on delete NO ACTION;
alter table "BILL" add constraint "bil_sf_fk" foreign key("training_session_fee") references "TRAINING_SESSION_FEE"("id") on update NO ACTION on delete NO ACTION;
alter table "PAYMENT" add constraint "pay_bil_fk" foreign key("bill") references "BILL"("id") on update NO ACTION on delete NO ACTION;
alter table "ACCOUNT_PROFILE" add constraint "prof_acc_fk" foreign key("account") references "ACCOUNT"("id") on update NO ACTION on delete NO ACTION;

-- !Downs

alter table "ACCOUNT_PROFILE" drop constraint "prof_acc_fk";
alter table "PAYMENT" drop constraint "pay_bil_fk";
alter table "BILL" drop constraint "bil_enr_fk";
alter table "BILL" drop constraint "bil_sf_fk";
alter table "ENROLLMENT" drop constraint "enr_acc_fk";
alter table "ENROLLMENT" drop constraint "enr_ses_fk";
alter table "TRAINING_SESSION_FEE" drop constraint "sf_ses_fk";
alter table "TRAINING_FEE" drop constraint "tf_tp_fk";
alter table "TRAINING_SESSION" drop constraint "ses_tp_fk";
alter table "TRAINING_UNIT" drop constraint "uni_tra_fk";
alter table "TRAINING_PLAN" drop constraint "tp_pla_fk";
alter table "TRAINING_PLAN" drop constraint "tp_tra_fk";
alter table "TRAINING" drop constraint "tra_pro_fk";
alter table "PLAN" drop constraint "pla_pro_fk";
alter table "PROGRAM" drop constraint "pro_dep_fk";
drop table "ACCOUNT_PROFILE";
drop table "PAYMENT";
drop table "BILL";
drop table "ENROLLMENT";
drop table "TRAINING_SESSION_FEE";
drop table "TRAINING_FEE";
drop table "ACCOUNT";
drop table "TRAINING_SESSION";
drop table "TRAINING_UNIT";
drop table "TRAINING_PLAN";
drop table "TRAINING";
drop table "PLAN";
drop table "PROGRAM";
drop table "DEPARTMENT";
