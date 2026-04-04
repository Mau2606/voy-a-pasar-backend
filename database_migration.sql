-- Agregar nuevos campos a la tabla users
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS access_type VARCHAR(30);
ALTER TABLE users ADD COLUMN IF NOT EXISTS expiration_date TIMESTAMP;

-- Actualizar estados existentes (opcional pero recomendado)
UPDATE users SET account_status = 'ACTIVE' WHERE account_status = 'PENDING';
UPDATE users SET account_status = 'INACTIVE' WHERE account_status = 'SUSPENDED';

-- Crear la tabla user_sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_timestamp TIMESTAMP NOT NULL,
    logout_timestamp TIMESTAMP,
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Crear la tabla quiz_attempts
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    chapter_id BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    start_time TIMESTAMP NOT NULL,
    seconds_used INTEGER,
    CONSTRAINT fk_quiz_attempts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_attempts_chapter FOREIGN KEY (chapter_id) REFERENCES chapters (id) ON DELETE CASCADE
);

-- Crear la tabla question_reports
CREATE TABLE IF NOT EXISTS question_reports (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    admin_notes TEXT,
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    CONSTRAINT fk_reports_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
);
