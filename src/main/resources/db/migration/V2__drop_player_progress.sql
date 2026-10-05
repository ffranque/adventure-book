-- The per-player progress feature was removed; V1 is left unchanged
-- because applied migrations must never be edited.
drop table player_progress;
drop sequence player_progress_seq;
