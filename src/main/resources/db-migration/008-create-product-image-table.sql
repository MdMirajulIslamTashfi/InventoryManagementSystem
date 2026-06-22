CREATE TABLE public.product_image (
    id          UUID        DEFAULT gen_random_uuid() NOT NULL,
    product_id  UUID        NOT NULL,
    image_url   VARCHAR(1000) NOT NULL,
    created_at  TIMESTAMP   DEFAULT NOW() NOT NULL,
    CONSTRAINT product_image_pkey PRIMARY KEY (id),
    CONSTRAINT product_image_product_id_fkey
        FOREIGN KEY (product_id)
        REFERENCES public.product(id)
        ON DELETE CASCADE
);