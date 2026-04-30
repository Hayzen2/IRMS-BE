package com.example.IRMS.modules.digital_ordering.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.IRMS.config.exceptions.ResourceNotFoundException;
import com.example.IRMS.modules.digital_ordering.dtos.MenuItemRequest;
import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import com.example.IRMS.modules.digital_ordering.models.MenuItemEntity;
import com.example.IRMS.modules.digital_ordering.repositories.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuManagementService {
    private final MenuRepository menuRepository;

    public List<MenuItemEntity> getAllMenuItems(boolean availableOnly) {
        if (availableOnly) {
            return menuRepository.findByIsAvailableTrue();
        } else {
            return menuRepository.findAll();
        }
    }

    public List<MenuItemEntity> getAllItemsByCategory(
        DishCategory category, boolean availableOnly
    ) 
    {
        if (availableOnly) {
            return menuRepository.findByDishCategoryAndIsAvailableTrue(category);
        } else {
            return menuRepository.findAllByDishCategory(category);
        }
    }

    public MenuItemEntity getMenuItemById(Long id) {
        return menuRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    // Add new menu item
    public MenuItemEntity addMenuItem(MenuItemRequest menuItem) {
        MenuItemEntity menuItemEntity = new MenuItemEntity();
        menuItemEntity.setName(menuItem.getName());
        menuItemEntity.setPrice(menuItem.getPrice());
        menuItemEntity.setDishCategory(menuItem.getDishCategory());
        menuItemEntity.setDescription(menuItem.getDescription());
        menuItemEntity.setStations(menuItem.getStationTypes());
        menuItemEntity.setEstimatedPrepMinutes(menuItem.getEstimatedPrepMinutes());
        menuItemEntity.setAvailable(menuItem.getIsAvailable());
        return menuRepository.save(menuItemEntity);
    }

    // Update existing menu item
    public MenuItemEntity updateMenuItem(Long id, MenuItemRequest menuItem) {
        return menuRepository.findById(id)
                .map(existingItem -> {
                    if (menuItem.getName() != null){
                        existingItem.setName(menuItem.getName());
                    }
                    if (menuItem.getPrice() != null) {
                        existingItem.setPrice(menuItem.getPrice());
                    }
                    if (menuItem.getDishCategory() != null) {
                        existingItem.setDishCategory(menuItem.getDishCategory());
                    }
                    if (menuItem.getDescription() != null) {
                        existingItem.setDescription(menuItem.getDescription());
                    }
                    if (menuItem.getStationTypes() != null) {
                        existingItem.setStations(menuItem.getStationTypes());
                    }
                    if (menuItem.getEstimatedPrepMinutes() != null) {
                        existingItem.setEstimatedPrepMinutes(menuItem.getEstimatedPrepMinutes());
                    }
                    if (menuItem.getIsAvailable() != null) {
                        existingItem.setAvailable(menuItem.getIsAvailable());
                    }
                    return menuRepository.save(existingItem);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    // Delete menu item
    public void deleteMenuItem(Long id) {
        menuRepository.deleteById(id);
    }
}
