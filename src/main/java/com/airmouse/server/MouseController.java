package com.airmouse.server;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Controls the system mouse using java.awt.Robot.
 * Supports relative movement, left click, and right click.
 */
public class MouseController {

    private final Robot robot;

    public MouseController() throws Exception {
        this.robot = new Robot();
    }

    /**
     * Moves the mouse cursor relative to its current position.
     * Accepts floating-point deltas and rounds to the nearest integer.
     */
    public void moveMouse(double dx, double dy) {
        Point current = MouseInfo.getPointerInfo().getLocation();
        int targetX = current.x + (int) Math.round(dx);
        int targetY = current.y + (int) Math.round(dy);
        robot.mouseMove(targetX, targetY);
    }

    /**
     * Performs a left mouse button click.
     */
    public void leftClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Performs a right mouse button click.
     */
    public void rightClick() {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }
}
