package com.example.IRMS.modules.digital_ordering.models;
import java.time.LocalDateTime;

import com.example.IRMS.modules.digital_ordering.enums.OrderItemProgressStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore 
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItemEntity menuItem;

    @Column(nullable=false)
    private int quantity;
    // Note like: "bring sauce on the side, bring desert first"
    @Column(nullable=true)
    private String specialInstructions; 
    @Column(nullable=true)
    private String allergyNotes;
    // note like: "no onions, extra cheese"
    @Column(nullable=true)
    private String customization; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemProgressStatus progressStatus = OrderItemProgressStatus.PENDING;

    @Column(nullable=true)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private int currentStationIndex = 0;

    @Column(nullable=true)
    private Integer actualPrepMinutes;

    @Column(nullable=true)
    private OrderItemProgressStatus previousProgressStatus;
}
