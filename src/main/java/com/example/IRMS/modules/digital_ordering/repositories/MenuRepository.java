package com.example.IRMS.modules.digital_ordering.repositories;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.IRMS.modules.digital_ordering.models.MenuItemEntity;
import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<MenuItemEntity, Long> {
    List<MenuItemEntity> findByDishCategory(DishCategory category);
    List<MenuItemEntity> findByIsAvailableTrue();
    List<MenuItemEntity> findByDishCategoryAndIsAvailableTrue(DishCategory category);
    List<MenuItemEntity> findAllByDishCategory(DishCategory category);
}
