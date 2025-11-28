package ru.itmo.lab3.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.math.BigDecimal;
public class HitResult {
    private final BigDecimal x;
    private final BigDecimal y;
    private final BigDecimal r;
    private final boolean hit;
    private Timestamp timestamp;

    public HitResult(BigDecimal x, BigDecimal y, BigDecimal r, boolean hit) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
        this.timestamp = Timestamp.from(Instant.now());
    }

    public BigDecimal getX() { return x; }
    public BigDecimal getY() { return y; }
    public BigDecimal getR() { return r; }
    public boolean isHit() { return hit; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}