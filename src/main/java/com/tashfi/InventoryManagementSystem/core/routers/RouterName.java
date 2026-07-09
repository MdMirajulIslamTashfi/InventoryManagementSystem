package com.tashfi.InventoryManagementSystem.core.routers;

public class RouterName {
    // Base URL
    public static final String BASE_URL = "/api";

    // PathVariable
    public static final String NAME = "/{name}";
    public static final String ID = "/{id}";
    public static final String PRODUCT_ID_URL = "/{productId}";
    public static final String IMAGE_ID_URL = "/{imageId}";

    // Customer
    public static final String CUSTOMER_BASE_URL = "/customers";
    public static final String CUSTOMER_REGISTER_URL = "/register";
    public static final String CUSTOMER_LOGIN_URL = "/login";
    public static final String CUSTOMER_UPDATE_URL = "/update";
    public static final String CUSTOMER_DELETE_URL = "/delete";

    // Categories
    public static final String CATEGORY_BASE_URL = "/categories";
    public static final String CATEGORY_ADD_URL = "/add";
    public static final String CATEGORY_SEARCH_URL = "/search";
    public static final String CATEGORY_UPDATE_URL = "/update";
    public static final String CATEGORY_DELETE_URL = "/delete";

    // Product
    public static final String PRODUCT_BASE_URL = "/products";
    public static final String PRODUCT_ADD_URL = "/add";
    public static final String PRODUCT_SEARCH_URL = "/search";
    public static final String PRODUCT_UPDATE_URL = "/update";
    public static final String PRODUCT_DELETE_URL = "/delete";

    // Product Images — same URL for GET / PUT / DELETE of a single image
    public static final String PRODUCT_IMAGE_URL = "/images";
}