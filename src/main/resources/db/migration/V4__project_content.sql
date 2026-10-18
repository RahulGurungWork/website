-- Real project texts, years and technologies, replacing the placeholders from V2 (2026-10-18).
-- Only years are known, so dates are 1 January of that year and the pages show the year only.
-- Slugs stay unchanged so existing URLs keep working.

-- Notion project management board, 2025–2026
update project set status = 'PAST', started_on = date '2025-01-01', ended_on = date '2026-01-01'
where slug = 'notion-workspace-prototype';

update project_translation t set
  title = 'Notion project management board',
  summary = 'An Eisenhower-inspired board in Notion that sets the priority of every project automatically.',
  description = 'The board builds on the Eisenhower matrix of urgency and importance and adds two more dimensions: engagement and value to the company. From these ratings Notion assigns each project its priority automatically, so the most worthwhile work comes first.

The idea started as a C# project. In Notion it became a workspace with several views of the same projects, each suited to a different question.'
from project p where t.project_id = p.id and p.slug = 'notion-workspace-prototype' and t.locale = 'en';

update project_translation t set
  title = 'Notion-Projektmanagement-Board',
  summary = 'Ein vom Eisenhower-Prinzip inspiriertes Board in Notion, das die Priorität jedes Projekts automatisch festlegt.',
  description = 'Das Board baut auf der Eisenhower-Matrix aus Dringlichkeit und Wichtigkeit auf und ergänzt zwei weitere Dimensionen: Engagement und Mehrwert für das Unternehmen. Aus diesen Bewertungen vergibt Notion jedem Projekt automatisch seine Priorität, damit die lohnendste Arbeit zuerst kommt.

Die Idee entstand als C#-Projekt. In Notion wurde daraus ein Arbeitsbereich mit mehreren Ansichten derselben Projekte, jede passend für eine andere Frage.'
from project p where t.project_id = p.id and p.slug = 'notion-workspace-prototype' and t.locale = 'de';

-- n8n workflow, 2026, ongoing
update project set status = 'CURRENT', started_on = date '2026-01-01', ended_on = null
where slug = 'n8n-workflow';

update project_translation t set
  summary = 'An automated n8n workflow that moves users from an Abacus Excel export into the company''s own application.',
  description = 'Users exported from Abacus as an Excel sheet need to end up in the company''s own application. The workflow reads the export and transfers the users there automatically.

It runs on a self-hosted n8n instance.'
from project p where t.project_id = p.id and p.slug = 'n8n-workflow' and t.locale = 'en';

update project_translation t set
  summary = 'Ein automatisierter n8n-Workflow, der Benutzer aus einem Excel-Export von Abacus in die firmeneigene Anwendung überführt.',
  description = 'Benutzer, die aus Abacus als Excel-Tabelle exportiert werden, müssen in die firmeneigene Anwendung gelangen. Der Workflow liest den Export und überträgt die Benutzer automatisch dorthin.

Er läuft auf einer selbst betriebenen n8n-Instanz.'
from project p where t.project_id = p.id and p.slug = 'n8n-workflow' and t.locale = 'de';

-- Console learning documentation generator, 2025
update project set status = 'PAST', started_on = date '2025-01-01', ended_on = null
where slug = 'lerndoku-generator';

update project_translation t set
  summary = 'A console program that guides you through a reflection for your learning documentation.',
  description = 'Apprentices in Switzerland keep a learning documentation and regularly reflect on what they learned. The program offers proven reflection models in a simple menu: Gibbs'' reflective cycle, Kolb''s learning cycle and Rolfe''s quick model, plus sets of motivation and lesson questions in random order, inspired by Leuchtturm1917.

You choose a model by its number and answer its questions step by step in the console.'
from project p where t.project_id = p.id and p.slug = 'lerndoku-generator' and t.locale = 'en';

update project_translation t set
  summary = 'Ein Konsolenprogramm, das durch eine Reflexion für die Lerndokumentation führt.',
  description = 'Lernende in der Schweiz führen eine Lerndokumentation und reflektieren regelmässig, was sie gelernt haben. Das Programm bietet bewährte Reflexionsmodelle in einem einfachen Menü: Gibbs'' Reflexionsmodell, Kolbs Lernzyklus und das schnelle Modell von Rolfe, dazu Motivations- und Unterrichtsfragen in zufälliger Reihenfolge, inspiriert von Leuchtturm1917.

Man wählt ein Modell über seine Nummer und beantwortet die Fragen Schritt für Schritt in der Konsole.'
from project p where t.project_id = p.id and p.slug = 'lerndoku-generator' and t.locale = 'de';

-- Technologies: replace the placeholders
insert into technology (name) values ('C#'), ('Abacus'), ('Excel')
on conflict (name) do nothing;

delete from project_technology
where project_id in (select id from project where slug in ('notion-workspace-prototype', 'n8n-workflow'));

insert into project_technology (project_id, technology_id)
select p.id, tech.id
from (values
  ('notion-workspace-prototype', 'Notion'),
  ('notion-workspace-prototype', 'C#'),
  ('n8n-workflow',               'n8n'),
  ('n8n-workflow',               'Abacus'),
  ('n8n-workflow',               'Excel')
) as pt (slug, technology)
join project p on p.slug = pt.slug
join technology tech on tech.name = pt.technology;

-- "REST API" was a placeholder and is no longer used
delete from technology where name = 'REST API'
  and not exists (select 1 from project_technology pt where pt.technology_id = technology.id);
