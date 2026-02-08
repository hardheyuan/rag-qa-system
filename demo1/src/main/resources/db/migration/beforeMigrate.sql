-- Flyway callback: execute before versioned migrations.
-- Purpose: make legacy schemas compatible with V2 migration (idx_user_role).

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 't_user'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 't_user' AND column_name = 'role'
        ) THEN
            ALTER TABLE t_user ADD COLUMN role VARCHAR(20) DEFAULT 'STUDENT';
        END IF;

        UPDATE t_user
        SET role = 'STUDENT'
        WHERE role IS NULL
           OR role = ''
           OR role NOT IN ('STUDENT', 'TEACHER', 'ADMIN');

        ALTER TABLE t_user ALTER COLUMN role SET NOT NULL;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'chk_user_role'
              AND conrelid = 't_user'::regclass
        ) THEN
            ALTER TABLE t_user
                ADD CONSTRAINT chk_user_role CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'));
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_indexes
            WHERE schemaname = 'public'
              AND tablename = 't_user'
              AND indexname = 'idx_user_role'
        ) THEN
            EXECUTE 'CREATE INDEX idx_user_role ON t_user(role)';
        END IF;
    END IF;
END $$;
