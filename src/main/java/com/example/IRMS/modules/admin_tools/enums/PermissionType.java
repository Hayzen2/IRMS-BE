package com.example.IRMS.modules.admin_tools.enums;

public enum PermissionType {
    // Digital ordering & menu management
    TAKE_ORDER,
    UPDATE_ORDER,
    CANCEL_ORDER,
    VIEW_MENU,
    UPDATE_MENU,
    APPLY_MENU_PRICE_UPDATE,
    APPLY_PROMOTION,
    ROUTE_ORDER_TO_KITCHEN,

    // Kitchen display & workflow
    VIEW_KDS,
    ORGANIZE_KDS_QUEUE,
    UPDATE_ORDER_PROGRESS,
    RECEIVE_KITCHEN_ALERTS,

    // Table & reservation management
    VIEW_TABLE_STATUS,
    UPDATE_TABLE_STATUS,
    MANAGE_RESERVATIONS,
    MANAGE_WAITLIST,
    SEND_RESERVATION_NOTIFICATIONS,

    // Billing, payments & receipts
    CREATE_BILL,
    PROCESS_PAYMENT,
    MANAGE_SPLIT_BILL,
    MANAGE_TIPS,
    ISSUE_REFUND,

    // Inventory monitoring
    UPDATE_INGREDIENT_USAGE,
    VIEW_STOCK_LEVELS,
    RECEIVE_STOCK_ALERTS,
    MANAGE_REORDER,

    // Analytics & reports
    VIEW_SALES_REPORTS,
    VIEW_OPERATIONAL_ANALYTICS,
    EXPORT_REPORTS,

    // Administrative tools
    MANAGE_USERS,
    MANAGE_ROLES,
    VIEW_AUDIT_LOGS,
    CONFIGURE_SYSTEM_SETTINGS
}
