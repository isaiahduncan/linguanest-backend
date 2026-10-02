-- GET /chats/{chatId}/messages filters exercises on chat_id (see
-- ExerciseRepository.findByChatIdOrderByCreatedAtAsc) - V1 indexed user_id but not chat_id,
-- so that query was doing a full table scan. Added here, not in V1, since an already-applied
-- migration is never edited (see backend-api-spec.md, Schema Migrations).
CREATE INDEX idx_exercises_chat_id ON exercises(chat_id);
