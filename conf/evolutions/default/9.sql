-- Alter ACCOUNT_PROFILE table

-- !Ups

alter table "ACCOUNT_PROFILE"
    drop column if exists "nic_number",
    drop column if exists "nic_date_of_issue",
    drop column if exists "nic_place_of_issue",
    drop column if exists "highest_academic_qualification",
    drop column if exists "year_of_issue",
    drop column if exists "english_grade_in_gceol",
    drop column if exists "mathematics_grade_in_gceol",
    drop column if exists "professional_qualification",
    drop column if exists "employment_status",
    drop column if exists "job_title",
    drop column if exists "years_of_experience",
    drop column if exists "employer_name",
    drop column if exists "employer_address";

-- !Downs

alter table "ACCOUNT_PROFILE"
    add column "nic_number" VARCHAR,
    add column "nic_date_of_issue" DATE,
    add column "nic_place_of_issue" VARCHAR,
    add column "highest_academic_qualification" VARCHAR,
    add column "year_of_issue" INTEGER,
    add column "english_grade_in_gceol" VARCHAR,
    add column "mathematics_grade_in_gceol" VARCHAR,
    add column "professional_qualification" VARCHAR,
    add column "employment_status" VARCHAR NOT NULL,
    add column "job_title" VARCHAR,
    add column "years_of_experience" INTEGER,
    add column "employer_name" VARCHAR,
    add column "employer_address" VARCHAR;
