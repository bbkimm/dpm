/*
 * Odometer.java
 */

package navigation;

import javax.swing.colorchooser.ColorSelectionModel;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta, WR, TR;

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;
	
	private int prevTachoL, prevTachoR, tachoL, tachoR, distL, distR;

	// lock object for mutual exclusion
	private Object lock;

	private static EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double WR, double TR) {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.WR = WR;
		this.TR = TR;
		lock = new Object();
	}


	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		
		
		while (true) {
			updateStart = System.currentTimeMillis();
			double distL, distR, deltaD, deltaT, dX, dY;
			
			tachoL = leftMotor.getTachoCount(); //get values from the motors
			tachoR = rightMotor.getTachoCount();
			distL = (Math.PI*WR*(tachoL-prevTachoL))/180;		// compute L and R wheel displacements
			distR = (Math.PI*WR*(tachoR-prevTachoR))/180;
			prevTachoL=tachoL;								// save tacho counts for next iteration
			prevTachoR=tachoR;
			deltaD = 0.5*(distL+distR);							// compute vehicle displacement
			deltaT = (180*((distL-distR)/TR))/Math.PI;							// compute change in heading									// update heading
		   
			
			
			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				
				theta += deltaT;
				dX = deltaD * Math.sin((Math.PI*theta)/180);						// compute X component of displacement
				dY = deltaD * Math.cos((Math.PI*theta)/180);						// compute Y component of displacement
				x = x + dX;											// update estimates of X and Y position
				y = y + dY;	
				
				
			}
			
			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}