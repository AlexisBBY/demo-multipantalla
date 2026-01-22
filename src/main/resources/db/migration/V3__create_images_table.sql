CREATE TABLE IF NOT EXISTS images (
  id BIGSERIAL PRIMARY KEY,
  original_name TEXT NOT NULL,
  content_type TEXT NOT NULL,
  data BYTEA NOT NULL
);
