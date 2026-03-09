package com.kmaleon.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "unit_conversions")
public class UnitConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_unit_id", nullable = false)
    private Unit fromUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_unit_id", nullable = false)
    private Unit toUnit;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal factor;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public Unit getFromUnit() { return fromUnit; }
    public void setFromUnit(Unit fromUnit) { this.fromUnit = fromUnit; }

    public Unit getToUnit() { return toUnit; }
    public void setToUnit(Unit toUnit) { this.toUnit = toUnit; }

    public BigDecimal getFactor() { return factor; }
    public void setFactor(BigDecimal factor) { this.factor = factor; }
}
