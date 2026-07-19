package com.airmouse.server;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Controls the system mouse using java.awt.Robot.
 * Optimized for low-latency touchpad input with sub-pixel accumulation.
 *
 * Drag support has been intentionally removed. It relied on a persistent
 * "is the button currently held down" flag shared across every client
 * connection, which could get stuck if a client ever disconnected
 * mid-drag — silently breaking left-click for the rest of the server's
 * lifetime. Removing drag removes that entire class of bug.
 */
public class MouseController {

    private final Robot robot;

    /** Accumulated fractional remainders for sub-pixel precision. */
    private double accumulatorX = 0.0;
    private double accumulatorY = 0.0;

    private double sensitivity = 1.5;

    public MouseController() throws Exception {
        this.robot = new Robot();
        // Eliminate Robot's internal delays for minimal latency
        this.robot.setAutoDelay(0);
        this.robot.setAutoWaitForIdle(false);
    }

    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Moves the mouse cursor relative to its current position.
     * Accumulates sub-pixel deltas so small movements are never lost.
     */
    public void moveMouse(double dx, double dy) {
        accumulatorX += (dx * sensitivity);
        accumulatorY += (dy * sensitivity);

        int moveX = (int) accumulatorX;
        int moveY = (int) accumulatorY;

        if (moveX != 0 || moveY != 0) {
            Point current = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(current.x + moveX, current.y + moveY);
            accumulatorX -= moveX;
            accumulatorY -= moveY;
        }
    }

    /**
     * Performs a left mouse button click (press immediately followed by
     * release — never left held down).
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

    /**
     * Scrolls the mouse wheel by the specified delta.
     */
    public void scroll(double delta) {
        // Robot.mouseWheel takes an int. Scale slightly if needed, but rounding usually suffices.
        int scrollAmount = (int) Math.round(delta);
        if (scrollAmount != 0) {
            robot.mouseWheel(scrollAmount);
        }
    }

    /**
     * Resets any accumulated sub-pixel movement remainder. Called when a
     * client disconnects so a fresh connection starts from a clean state.
     */
    public void reset() {
        accumulatorX = 0.0;
        accumulatorY = 0.0;
    }
}