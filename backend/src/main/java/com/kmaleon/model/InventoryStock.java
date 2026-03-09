package com.kmaleon.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_stock")
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "min_quantity", nullable = false, precision = 12, scale = 6)
    private BigDecimal minQuantity = BigDecimal.ZERO;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getMinQuantity() { return minQuantity; }
    public void setMinQuantity(BigDecimal minQuantity) { this.minQuantity = minQuantity; }
}
