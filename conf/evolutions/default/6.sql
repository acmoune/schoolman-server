-- Alter ENROLLMENT table

-- !Ups

alter table "ENROLLMENT"
  drop column "status";

-- !Downs

alter table "ENROLLMENT"
  add column "status" VARCHAR NOT NULL;
