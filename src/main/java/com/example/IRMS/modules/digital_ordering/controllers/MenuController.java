package com.example.IRMS.modules.digital_ordering.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.IRMS.modules.digital_ordering.dtos.MenuItemRequest;
import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import com.example.IRMS.modules.digital_ordering.models.MenuItemEntity;
import com.example.IRMS.modules.digital_ordering.services.MenuManagementService;
import com.example.IRMS.utils.ApiResponse; 

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;


// RequestParam: for optional modifiers like filtering, search, pagination, sorting, flags
// (availableOnly=true to get only available items)
// PathVariable: when you want to access a specific resource by its identifier 
// (/item/{id} to get a menu item by its ID)

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuManagementService menuManagerService;

    @Operation(summary = "Get all menu items", description = "Returns all menu items, with optional filtering by availability")
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER', 'PERM_UPDATE_MENU')")
    public ResponseEntity<ApiResponse<List<MenuItemEntity>>> getAllMenuItems(
            @RequestParam(defaultValue = "false") boolean availableOnly) {
            List<MenuItemEntity> data = menuManagerService.getAllMenuItems(availableOnly);
        return ResponseEntity.ok(new ApiResponse<>(200, "Menu items fetched successfully", data));
    }

    @Operation(summary = "Get menu items by category", description = "Returns menu items for a category, with optional availability filtering")
    @GetMapping("/items-by-category/{category}")
    @PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER', 'PERM_UPDATE_MENU', 'PERM_VIEW_MENU')")
    public ResponseEntity<ApiResponse<List<MenuItemEntity>>> getMenuItemsByCategory(
            @PathVariable DishCategory category,
            @RequestParam(defaultValue = "false") boolean availableOnly) {
        List<MenuItemEntity> data = menuManagerService.getAllItemsByCategory(category, availableOnly);
        return ResponseEntity.ok(new ApiResponse<>(200, "Menu items fetched successfully", data));
    }

    @Operation(summary = "Get menu item by ID", description = "Returns one menu item by its ID")
    @GetMapping("/item/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_TAKE_ORDER', 'PERM_UPDATE_MENU', 'PERM_VIEW_MENU')")
    public ResponseEntity<ApiResponse<MenuItemEntity>> getMenuItemById(@PathVariable Long id) {
        MenuItemEntity data = menuManagerService.getMenuItemById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Menu item fetched successfully", data));
    }

    @Operation(summary = "Update menu item", description = "Updates editable fields of an existing menu item")
    @PatchMapping("/update-item/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_UPDATE_MENU')")
    public ResponseEntity<ApiResponse<MenuItemEntity>> updateMenuItem(
            @PathVariable Long id,
            @RequestBody MenuItemRequest menuItem) {
        MenuItemEntity data = menuManagerService.updateMenuItem(id, menuItem);
        return ResponseEntity.ok(new ApiResponse<>(200, "Menu item updated successfully", data));
    }

    @Operation(summary = "Add menu item", description = "Creates a new menu item")
    @PostMapping("/add-item")
    @PreAuthorize("hasAnyAuthority('PERM_UPDATE_MENU')")
    public ResponseEntity<ApiResponse<MenuItemEntity>> addMenuItem(@RequestBody MenuItemRequest menuItem) {
        MenuItemEntity data = menuManagerService.addMenuItem(menuItem);
        return ResponseEntity.ok(new ApiResponse<>(200, "Menu item added successfully", data));
    }

    @Operation(summary = "Delete menu item", description = "Deletes an existing menu item by ID")
    @PostMapping("/delete-item/{id}")
    @PreAuthorize("hasAnyAuthority('PERM_UPDATE_MENU')")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        menuManagerService.deleteMenuItem(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Menu item deleted successfully", null));
    }

}
