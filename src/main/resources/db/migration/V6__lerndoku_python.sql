-- The learning documentation generator is written in Python, not Java (placeholder from V2).

insert into technology (name) values ('Python') on conflict (name) do nothing;

delete from project_technology
where project_id = (select id from project where slug = 'lerndoku-generator');

insert into project_technology (project_id, technology_id)
select p.id, t.id from project p, technology t
where p.slug = 'lerndoku-generator' and t.name = 'Python';

-- "Java" was only used by that placeholder
delete from technology where name = 'Java'
  and not exists (select 1 from project_technology pt where pt.technology_id = technology.id);
