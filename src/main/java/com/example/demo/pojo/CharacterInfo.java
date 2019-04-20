package com.example.demo.pojo;

import java.io.Serializable;

public class CharacterInfo implements Serializable {
    private String value;
    private int x;
    private int y;

    /**
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y y
     */
    public void setY(int y) {
        this.y = y;
    }
}
