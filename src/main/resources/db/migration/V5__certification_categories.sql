-- Group certifications on the certificates page (2026-10-18).
-- New rows default to PROFESSIONAL; set the category explicitly when seeding.

alter table certification
  add column category varchar(30) not null default 'PROFESSIONAL'
  check (category in ('LANGUAGE_AND_SCHOOL', 'TECHNICAL', 'PROFESSIONAL'));

update certification set category = 'LANGUAGE_AND_SCHOOL'
where issuer in ('Cambridge University Press & Assessment', 'gibb Berufsfachschule Bern');

update certification set category = 'TECHNICAL'
where issuer = 'Codecademy' and name in ('Learn GitHub: Introduction', 'Introduction to UI and UX Design',
  'Operating Systems: Introduction', 'Intro to Generative AI', 'Learn Bootstrap', 'Learn Bash Scripting');

-- Everything else (professional-skills courses, the BAND Lead Link role) keeps PROFESSIONAL.
