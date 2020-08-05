-- Alter BILL table

-- !Ups

alter table "BILL"
  drop column if exists "status",
  drop column if exists "deadline";

-- !Downs

alter table "TRAINING_SESSION"
  add column "status" VARCHAR NOT NULL,
  add column "deadline" DATE;
