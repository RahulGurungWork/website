-- Certifications, read from the certificate PDFs (2026-10-17). School reports are deliberately not listed.
-- Dates are completion or issue dates. No credential links yet: Codecademy codes and the Cambridge
-- certificate number are not public URLs. Order: language and school, role, technical courses,
-- professional skills; newest first within each group.

insert into certification (name, issuer, issued_on, sort_order) values
  ('C1 Advanced (Certificate in Advanced English)', 'Cambridge University Press & Assessment', date '2026-07-19', 10),
  ('English course, level C1 (52 lessons)',          'gibb Berufsfachschule Bern',               date '2026-06-19', 20),
  ('Lead Link, Kreis BI-Lernende',                   'BAND',                                     null,              30),

  ('Learn GitHub: Introduction',                     'Codecademy', date '2025-08-07', 100),
  ('Introduction to UI and UX Design',               'Codecademy', date '2025-07-08', 110),
  ('Operating Systems: Introduction',                'Codecademy', date '2025-07-01', 120),
  ('Intro to Generative AI',                         'Codecademy', date '2025-06-23', 130),
  ('Learn Bootstrap',                                'Codecademy', date '2024-07-26', 140),
  ('Learn Bash Scripting',                           'Codecademy', date '2023-05-01', 150),

  ('Set Your Workspace Up For Success',              'Codecademy', date '2026-09-13', 200),
  ('Writing with a Professional Mindset',            'Codecademy', date '2026-08-13', 210),
  ('Product Management: Communication for Product Managers', 'Codecademy', date '2026-08-10', 220),
  ('Saving Time by Setting Goals',                   'Codecademy', date '2026-03-24', 230),
  ('Telling a Business Story',                       'Codecademy', date '2026-02-19', 240),
  ('Delivering Feedback That Encourages Growth',     'Codecademy', date '2026-02-17', 250),
  ('Expanding Your Communication Skill Set',         'Codecademy', date '2026-02-17', 260),
  ('Mastering OKRs: Writing, Measuring, and Achieving Success', 'Codecademy', date '2026-02-03', 270),
  ('Influencing without Authority',                  'Codecademy', date '2025-10-09', 280),
  ('Get Organized and Stop Procrastinating',         'Codecademy', date '2025-07-03', 290),
  ('Developing Personal Accountability',             'Codecademy', date '2025-07-01', 300),
  ('Building Success through Self-improvement',      'Codecademy', date '2025-06-27', 310),
  ('New Project Manager Essentials',                 'Codecademy', date '2024-09-02', 320),
  ('Be Productive by Working Efficiently',           'Codecademy', date '2024-07-23', 330),
  ('Building and Delivering a Great Virtual Presentation', 'Codecademy', date '2024-07-23', 340),
  ('Coping in a Toxic Workplace',                    'Codecademy', date '2024-07-23', 350),
  ('Learn What to Learn',                            'Codecademy', date '2024-07-23', 360);
