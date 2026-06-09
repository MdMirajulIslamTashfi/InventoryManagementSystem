package com.tashfi.InventoryManagementSystem.core.routers;

public class RouterName {
    // Customer
    public static final String CUSTOMER_BASE_URL = "/api/customers";
    public static final String CUSTOMER_REGISTER_URL = "/api/customers/register";
    public static final String CUSTOMER_LOGIN_URL = "/api/customers/login";

    // Categories
    public static final String CATEGORY_BASE_URL = "/api/categories";
    public static final String CATEGORY_ADD_URL = "/api/categories/add";
    public static final String CATEGORY_SEARCH_BY_NAME_URL = "/api/categories/search/{name}";
    public static final String CATEGORY_UPDATE_URL = "/api/categories/update/{name}";
    public static final String CATEGORY_DELETE_URL = "/api/categories/delete/{name}";

    // Product
    public static final String PRODUCT_BASE_URL = "/api/products";
    public static final String PRODUCT_ADD_URL = "/api/products/add";
    public static final String PRODUCT_SEARCH_BY_NAME_URL = "/api/products/search/{name}";
    public static final String PRODUCT_UPDATE_URL = "/api/products/update/{name}";
    public static final String PRODUCT_DELETE_URL = "/api/products/delete/{name}";
}