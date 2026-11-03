ALTER TABLE payments
    ADD COLUMN payment_method VARCHAR(30)
        NOT NULL DEFAULT 'LEGACY';

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_payment_method
        CHECK (
            payment_method IN (
                'UPI',
                'CARD',
                'DIGITAL_WALLET',
                'LEGACY'
            )
        );