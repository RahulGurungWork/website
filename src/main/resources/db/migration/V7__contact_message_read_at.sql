-- Admin inbox (step A4, 2026-10-30): when the admin first opened a message; null = unread.
-- Existing messages count as unread.
alter table contact_message add column read_at timestamptz;
