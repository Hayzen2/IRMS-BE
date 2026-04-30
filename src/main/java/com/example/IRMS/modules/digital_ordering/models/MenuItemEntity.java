package com.example.IRMS.modules.digital_ordering.models;
import java.util.List;

import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import com.example.IRMS.modules.digital_ordering.enums.StationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "menu_items")
@Data
public class MenuItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private List<StationType> stations;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private DishCategory dishCategory;

    // Use Integer instead of int to allow for null values if estimated prep time is not set
    private Integer estimatedPrepMinutes;
    @Column(nullable=false)
    private double price;
    @Column(nullable=false)
    private boolean isAvailable;
    private String description;
}
