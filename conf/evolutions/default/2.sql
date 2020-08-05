-- Alter TRAINING_SESSION table

-- !Ups

alter table "TRAINING_SESSION"
  drop column if exists "duration_unit",
  drop column if exists "duration";

alter table "TRAINING_SESSION"
  add column "expectedEndDate" date;

-- !Downs

alter table "TRAINING_SESSION"
  add column "duration_unit" varchar,
  add column "duration" double precision;

alter table "TRAINING_SESSION"
  drop column if exists "expectedEndDate";
