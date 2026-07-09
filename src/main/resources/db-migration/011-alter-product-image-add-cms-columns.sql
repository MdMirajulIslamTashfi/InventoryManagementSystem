ALTER TABLE public.product_image
    ADD COLUMN IF NOT EXISTS product_sku    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS product_name   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS image_name     VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_thumbnail   BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS thumbnail_url  VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS created_by     VARCHAR(50),
    ADD COLUMN IF NOT EXISTS updated_at     TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by     VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_product_image_product_id ON public.product_image(product_id);
