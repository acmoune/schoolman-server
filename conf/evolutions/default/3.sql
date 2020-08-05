-- Add super user

-- !Ups

insert into "ACCOUNT" (email, password, role, locked, full_name) values('super@us.er', '$2a$12$1HmuSoom30pWooB6eXuVDOIj.pVVGafqGfAReYONXL8MHvvfpygyW', 'Manager', false, 'Super User');

-- !Downs

delete from "ACCOUNT" where email = 'super@us.er';
