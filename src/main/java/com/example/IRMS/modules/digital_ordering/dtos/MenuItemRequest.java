package com.example.IRMS.modules.digital_ordering.dtos;

import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import com.example.IRMS.modules.digital_ordering.enums.StationType;
import java.util.List;
import lombok.Data;

@Data
public class MenuItemRequest {
    private boolean available;
    private Double price;
    private String description;
    private DishCategory dishCategory;
    private String name;
    private Integer estimatedPrepMinutes;
    private List<StationType> stationTypes;
    private Boolean isAvailable;
}
