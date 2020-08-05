-- Add position columns

-- !Ups

alter table "TRAINING_SESSION_FEE" add column "position" BIGINT;

-- !Downs

alter table "TRAINING_SESSION_FEE" drop column "position";