-- Release 1 showcase projects (docs/hermes/02-requirements.md).
-- Titles and summaries come from docs/design/home-mockup.html and are still placeholders:
-- the real texts, screenshots and alt texts arrive in a later migration (content preparation week).
-- Certifications are not seeded yet: the mockup only has examples.

insert into technology (name) values
  ('Java'),
  ('n8n'),
  ('Notion'),
  ('REST API');

insert into project (slug, status, sort_order, published) values
  ('notion-workspace-prototype', 'CURRENT', 10, true),
  ('n8n-workflow',               'CURRENT', 20, true),
  ('lerndoku-generator',         'CURRENT', 30, true);

insert into project_translation (project_id, locale, title, summary, description)
select p.id, t.locale, t.title, t.summary, t.description
from (values
  ('notion-workspace-prototype', 'en', 'Notion workspace prototype',
    'A project management system in Notion: tasks, priorities and progress in one place.',
    'A project management system in Notion: tasks, priorities and progress in one place. Full description to follow.'),
  ('notion-workspace-prototype', 'de', 'Notion-Workspace-Prototyp',
    'Ein Projektmanagement-System in Notion: Aufgaben, Prioritäten und Fortschritt an einem Ort.',
    'Ein Projektmanagement-System in Notion: Aufgaben, Prioritäten und Fortschritt an einem Ort. Ausführliche Beschreibung folgt.'),
  ('n8n-workflow', 'en', 'n8n workflow',
    'An automated n8n process that moves data between two systems and replaces manual steps.',
    'An automated n8n process that moves data between two systems and replaces manual steps. Full description to follow.'),
  ('n8n-workflow', 'de', 'n8n-Workflow',
    'Ein automatisierter Ablauf mit n8n, der Daten zwischen zwei Systemen überträgt und manuelle Schritte ersetzt.',
    'Ein automatisierter Ablauf mit n8n, der Daten zwischen zwei Systemen überträgt und manuelle Schritte ersetzt. Ausführliche Beschreibung folgt.'),
  ('lerndoku-generator', 'en', 'Learning documentation generator',
    'A tool that turns notes into structured learning documentation.',
    'A tool that turns notes into structured learning documentation. Full description to follow.'),
  ('lerndoku-generator', 'de', 'Lerndokumentations-Generator',
    'Ein Werkzeug, das aus Notizen strukturierte Lerndokumentationen erzeugt.',
    'Ein Werkzeug, das aus Notizen strukturierte Lerndokumentationen erzeugt. Ausführliche Beschreibung folgt.')
) as t (slug, locale, title, summary, description)
join project p on p.slug = t.slug;

insert into project_technology (project_id, technology_id)
select p.id, tech.id
from (values
  ('notion-workspace-prototype', 'Notion'),
  ('n8n-workflow',               'n8n'),
  ('n8n-workflow',               'REST API'),
  ('lerndoku-generator',         'Java')
) as pt (slug, technology)
join project p on p.slug = pt.slug
join technology tech on tech.name = pt.technology;
