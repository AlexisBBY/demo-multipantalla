CREATE TABLE IF NOT EXISTS contact_message (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(70) NOT NULL,
  email VARCHAR(254) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  birth_date DATE NOT NULL,
  message VARCHAR(500) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_contact_message_created_at
  ON contact_message (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_contact_message_full_name
  ON contact_message (full_name);

CREATE INDEX IF NOT EXISTS idx_contact_message_email
  ON contact_message (email);

CREATE INDEX IF NOT EXISTS idx_contact_message_phone
  ON contact_message (phone);