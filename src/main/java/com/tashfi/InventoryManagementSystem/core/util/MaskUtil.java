package com.tashfi.InventoryManagementSystem.core.util;

public class MaskUtil {
    /** Masks all but the last {@code visible} characters with '*'. Short values are returned as-is. */
    public static String maskKeepLast(String value, int visible) {
        if (value == null) return null;
        if (value.length() <= visible) return value;
        int maskCount = value.length() - visible;
        return "*".repeat(maskCount) + value.substring(maskCount);
    }

    /** Masks the mobile, leaving the last 3 digits visible, e.g. "***********000". */
    public static String maskContact(String mobile) {
        return maskKeepLast(mobile, 3);
    }

    /**
     * Masks the local part of an email, leaving the first and last chars before '@' visible and also the
     * domain but keeping the TLD intact, e.g. "rafiyad@example.com" → "r*****d@*******.com".
     */
    public static String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 0) return maskKeepLast(email, 4);

        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        return maskLocal(local) + "@" + maskDomain(domain);
    }

    /** Keeps first and last char visible, masks the rest, e.g. "support" -> "s*****t". */
    private static String maskLocal(String local) {
        if (local.length() <= 2) return local;
        int maskCount = local.length() - 2;
        return local.charAt(0) + "*".repeat(maskCount) + local.charAt(local.length() - 1);
    }

    /** Masks the domain name but keeps the TLD, e.g. "example.com" -> "*******.com". */
    private static String maskDomain(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot <= 0) return "*".repeat(domain.length());
        String name = domain.substring(0, lastDot);
        String tld = domain.substring(lastDot); // includes '.'
        return "*".repeat(name.length()) + tld;
    }
}