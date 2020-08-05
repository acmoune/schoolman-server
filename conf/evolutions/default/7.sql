-- Add position columns

-- !Ups

alter table "DEPARTMENT" add column "position" BIGINT;
alter table "PROGRAM" add column "position" BIGINT;
alter table "TRAINING" add column "position" BIGINT;
alter table "TRAINING_UNIT" add column "position" BIGINT;
alter table "TRAINING_FEE" add column "position" BIGINT;

-- !Downs

alter table "DEPARTMENT" drop column "position";
alter table "PROGRAM" drop column "position";
alter table "TRAINING" drop column "position";
alter table "TRAINING_UNIT" drop column "position";
alter table "TRAINING_FEE" drop column "position";
