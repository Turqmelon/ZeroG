package com.turqmelon.ZeroG;


public class GravLock {

    private double gravity;
    private long expiration;

    public GravLock(double gravity, long expiration) {
        this.gravity = gravity;
        this.expiration = expiration;
    }

    public double getGravity() {
        return gravity;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
