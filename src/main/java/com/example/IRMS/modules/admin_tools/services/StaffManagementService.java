package com.example.IRMS.modules.admin_tools.services;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.IRMS.modules.admin_tools.enums.PermissionType;
import com.example.IRMS.modules.admin_tools.enums.RoleType;

@Service
public class StaffManagementService {

	public boolean hasPermission(RoleType role, PermissionType permission) {
		return permissionsForRole(role).contains(permission);
	}

	public boolean hasPermission(String roleName, PermissionType permission) {
		if (roleName == null || roleName.isBlank()) {
			return false;
		}

		// Normalize role name by removing "ROLE_" prefix and converting to uppercase
		String normalized = roleName.replace("ROLE_", "").toUpperCase();
		try {
			return hasPermission(RoleType.valueOf(normalized), permission);
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	// Throws SecurityException if role does not have permission
	public void assertPermission(RoleType role, PermissionType permission) {
		if (!hasPermission(role, permission)) {
			throw new SecurityException("Role " + role + " cannot perform " + permission);
		}
	}

	// Get permissions for a role
	public Set<PermissionType> permissionsForRole(RoleType role) {
		return switch (role) {
			case MANAGER -> EnumSet.allOf(PermissionType.class);
			case SERVER -> EnumSet.of(
					PermissionType.VIEW_MENU,
					PermissionType.TAKE_ORDER,
						PermissionType.UPDATE_ORDER,
						PermissionType.CANCEL_ORDER,
            PermissionType.ROUTE_ORDER_TO_KITCHEN,
						PermissionType.VIEW_KDS,
            PermissionType.UPDATE_ORDER_PROGRESS,
						PermissionType.VIEW_TABLE_STATUS,
						PermissionType.UPDATE_TABLE_STATUS,
						PermissionType.MANAGE_RESERVATIONS,
						PermissionType.MANAGE_WAITLIST,
						PermissionType.SEND_RESERVATION_NOTIFICATIONS);
			case CASHIER -> EnumSet.of(
						PermissionType.VIEW_MENU,
						PermissionType.ROUTE_ORDER_TO_KITCHEN,
						PermissionType.CREATE_BILL,
						PermissionType.PROCESS_PAYMENT,
						PermissionType.MANAGE_SPLIT_BILL,
						PermissionType.MANAGE_TIPS,
						PermissionType.ISSUE_REFUND);
			case CHEF -> EnumSet.of(
					PermissionType.VIEW_MENU,
					PermissionType.VIEW_KDS,
						PermissionType.ORGANIZE_KDS_QUEUE,
					PermissionType.UPDATE_ORDER_PROGRESS,
						PermissionType.RECEIVE_KITCHEN_ALERTS,
						PermissionType.UPDATE_INGREDIENT_USAGE,
						PermissionType.VIEW_STOCK_LEVELS,
						PermissionType.RECEIVE_STOCK_ALERTS);
		};
	}
}