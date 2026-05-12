package com.example.IRMS.modules.digital_ordering.models;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.IRMS.modules.admin_tools.models.UserEntity;
import com.example.IRMS.modules.digital_ordering.enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private UserEntity staff;

    private Long tableId;
    private LocalDateTime orderTime;
    private LocalDateTime completedAt;
    private Integer actualPrepMinutes;
    private boolean nearDeadlineNotified;
    private boolean overdueNotified;

    // Assuming one order can have multiple items
    // CascadeType.ALL so when an OrderEntity is saved, its associated OrderItem are also saved
    // orphanRemoval=true to automatically delete OrderItemEntity from the database when
    // they are removed from the items list in OrderEntity
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
