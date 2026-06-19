ALTER TABLE IF EXISTS fund_disbursements
    ADD COLUMN IF NOT EXISTS request_sync_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS request_sync_error VARCHAR(500),
    ADD COLUMN IF NOT EXISTS request_synced_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS request_sync_attempts INTEGER;

UPDATE fund_disbursements
SET request_sync_status = COALESCE(request_sync_status, 'SYNCED'),
    request_sync_attempts = COALESCE(request_sync_attempts, 0)
WHERE request_sync_status IS NULL
   OR request_sync_attempts IS NULL;

ALTER TABLE IF EXISTS fund_disbursements
    ALTER COLUMN request_sync_status SET DEFAULT 'PENDING',
    ALTER COLUMN request_sync_attempts SET DEFAULT 0,
    ALTER COLUMN request_sync_status SET NOT NULL,
    ALTER COLUMN request_sync_attempts SET NOT NULL;
