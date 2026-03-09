package com.kmaleon.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal quantity;

    @Column(name = "quantity_base", nullable = false, precision = 12, scale = 6)
    private BigDecimal quantityBase;

    @Column(name = "movement_type", nullable = false, length = 20)
    private String movementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_from")
    private Location locationFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_to")
    private Location locationTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getQuantityBase() { return quantityBase; }
    public void setQuantityBase(BigDecimal quantityBase) { this.quantityBase = quantityBase; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public Location getLocationFrom() { return locationFrom; }
    public void setLocationFrom(Location locationFrom) { this.locationFrom = locationFrom; }

    public Location getLocationTo() { return locationTo; }
    public void setLocationTo(Location locationTo) { this.locationTo = locationTo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
