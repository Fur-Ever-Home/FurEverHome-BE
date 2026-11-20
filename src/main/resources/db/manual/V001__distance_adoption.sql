-- Additive migration for PostgreSQL. Run once before starting this BE version.
-- No existing donations are assigned to animals or marked as confirmed.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';
ALTER TABLE animale ADD COLUMN IF NOT EXISTS necesar_donatii_lunar NUMERIC(12, 2)
    CHECK (necesar_donatii_lunar > 0);
ALTER TABLE donatii_bani ADD COLUMN IF NOT EXISTS id_animal BIGINT
    REFERENCES animale(id_animal);
ALTER TABLE donatii_bani ADD COLUMN IF NOT EXISTS status_plata VARCHAR(30);
CREATE INDEX IF NOT EXISTS idx_donatii_animal_confirmate_data
    ON donatii_bani (id_animal, data_donatie)
    WHERE status_plata = 'confirmata';
COMMIT;
