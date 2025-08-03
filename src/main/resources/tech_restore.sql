CREATE TABLE
  public.users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    password text NOT NULL,
    phone character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone NULL
  );

ALTER TABLE
  public.users
ADD
  CONSTRAINT users_pkey PRIMARY KEY (id)


CREATE TABLE
  public.shop (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NULL,
    description character varying(250) NOT NULL,
    email character varying(255) NOT NULL,
    name character varying(50) NOT NULL,
    password character varying(512) NOT NULL,
    phone character varying(20) NOT NULL,
    rating numeric(10, 2) NULL,
    updated_at timestamp(6) without time zone NULL,
    verified boolean NOT NULL
  );

ALTER TABLE
  public.shop
ADD
  CONSTRAINT shop_pkey PRIMARY KEY (id)


  CREATE TABLE
  public.review (
    review_id uuid NOT NULL,
    comment
      character varying(255) NOT NULL,
      created_at timestamp(6) without time zone NULL,
      rating integer NOT NULL,
      shop_id uuid NULL,
      user_id uuid NULL
  );

ALTER TABLE
  public.review
ADD
  CONSTRAINT review_pkey PRIMARY KEY (review_id)


CREATE TABLE
  public.repair_request (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NULL,
    delivery_address_id uuid NULL,
    delivery_method character varying(255) NULL,
    description character varying(255) NULL,
    device_category character varying(255) NULL,
    payment_method character varying(255) NULL,
    price numeric(10, 2) NULL,
    shop_id uuid NULL,
    user_id uuid NULL
  );

ALTER TABLE
  public.repair_request
ADD
  CONSTRAINT repair_request_pkey PRIMARY KEY (id)


CREATE TABLE
  public.repair_payment (
    id uuid NOT NULL,
    amount numeric(10, 2) NULL,
    created_at timestamp(6) without time zone NULL,
    payment_method character varying(255) NULL,
    payment_reference character varying(255) NULL,
    payment_status character varying(255) NULL,
    repair_request_id uuid NULL,
    user_id uuid NULL
  );

ALTER TABLE
  public.repair_payment
ADD
  CONSTRAINT repair_payment_pkey PRIMARY KEY (id)



CREATE TABLE
  public.product (
    id uuid NOT NULL,
    category_id uuid NULL,
    condition character varying(255) NULL,
    created_at timestamp(6) without time zone NULL,
    description character varying(255) NULL,
    image_url character varying(255) NULL,
    name character varying(255) NOT NULL,
    price numeric(10, 2) NULL,
    shop_id uuid NULL,
    stock integer NULL
  );

ALTER TABLE
  public.product
ADD
  CONSTRAINT product_pkey PRIMARY KEY (id)



CREATE TABLE
  public.orders (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NULL,
    delivery_address_id uuid NULL,
    payment_method character varying(255) NULL,
    status character varying(255) NULL,
    total_price numeric(10, 2) NULL,
    user_id uuid NULL
  );

ALTER TABLE
  public.orders
ADD
  CONSTRAINT orders_pkey PRIMARY KEY (id)


CREATE TABLE
  public.order_payment (
    id uuid NOT NULL,
    amount numeric(10, 2) NULL,
    created_at timestamp(6) without time zone NULL,
    order_id uuid NULL,
    payment_method character varying(255) NULL,
    payment_reference character varying(255) NULL,
    payment_status character varying(255) NULL,
    user_id uuid NULL
  );

ALTER TABLE
  public.order_payment
ADD
  CONSTRAINT order_payment_pkey PRIMARY KEY (id)



CREATE TABLE
  public.order_item (
    id uuid NOT NULL,
    device_id uuid NULL,
    order_id uuid NULL,
    price_at_checkout numeric(10, 2) NULL,
    quantity integer NULL
  );

ALTER TABLE
  public.order_item
ADD
  CONSTRAINT order_item_pkey PRIMARY KEY (id)


CREATE TABLE
  public.category (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NULL,
    name character varying(255) NOT NULL
  );

ALTER TABLE
  public.category
ADD
  CONSTRAINT category_pkey PRIMARY KEY (id)



CREATE TABLE
  public.address (
    id uuid NOT NULL,
    building character varying(50) NOT NULL,
    city character varying(100) NOT NULL,
    is_default boolean NOT NULL,
    notes text NOT NULL,
    state character varying(100) NOT NULL,
    street text NOT NULL,
    user_id uuid NULL
  );

ALTER TABLE
  public.address
ADD
  CONSTRAINT address_pkey PRIMARY KEY (id)