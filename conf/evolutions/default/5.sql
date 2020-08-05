-- Alter TRAINING_SESSION AGAIN table

-- !Ups

alter table "TRAINING_SESSION"
  drop column "expectedEndDate";

-- !Downs

alter table "TRAINING_SESSION"
  add column "expectedEndDate" date;
