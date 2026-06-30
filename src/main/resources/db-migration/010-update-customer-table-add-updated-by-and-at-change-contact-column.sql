--changeset tashfi:add-updated-by-updated-at-to-customer
ALTER TABLE customer ADD COLUMN updated_by VARCHAR(20);
ALTER TABLE customer ADD COLUMN updated_at TIMESTAMP;

--rollback ALTER TABLE customer DROP COLUMN updated_by;
--rollback ALTER TABLE customer DROP COLUMN updated_at;

--changeset tashfi:alter-customer-contact-unique-length
ALTER TABLE customer ALTER COLUMN contact TYPE VARCHAR(14);
ALTER TABLE customer ADD CONSTRAINT uq_customer_contact UNIQUE (contact);

--rollback ALTER TABLE customer DROP CONSTRAINT uq_customer_contact;
--rollback ALTER TABLE customer ALTER COLUMN contact TYPE VARCHAR(255);