/*
 * Group 51
 * Brian Kim-Lim (260636766)
 * Jason Dias (260617554)
 * 
 * Lab 3 - Navigation
 */

package navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class Navigation extends Thread {

	private static int distance;

	// static variables
	private static final int FWD_SPEED = 210;
	private static final int ROTATE_SPEED = 150;

	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static final EV3MediumRegulatedMotor usMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

	private static boolean navigating = false, completed = false, turning = false, avoidance;
	private static double path[][];

	private static double TRACK;
	private static double WHEEL_RADIUS;

	public boolean done;

	// class constructor
	public Navigation(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double[][] path, double TRACK, double WHEEL_RADIUS, boolean avoidance) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.path = path;
		this.TRACK = TRACK;
		this.WHEEL_RADIUS = WHEEL_RADIUS;
		this.avoidance = avoidance;

		// ensure motors stopped
		leftMotor.stop();
		rightMotor.stop();

		leftMotor.setAcceleration(2200);
		rightMotor.setAcceleration(2200);

	}

	public void run() {
		// Determine which path to take:
		//Path 2
		if (avoidance) {
			travelTo(0, 60);
			travelTo(60, 0);
		//Path 1
		} else {
			travelTo(60, 30);
			travelTo(30, 30);
			travelTo(30, 60);
			travelTo(60, 0);
		}
		this.done = true;
	}

	private static void travelTo(double xDest, double yDest) {

		while (!completed) {
			// get offset between current location and destination
			double x = xDest - odometer.getX();
			double y = yDest - odometer.getY();

			// atan2 returns the theta angle from converting from rectangular
			// (x,y) coord to polar i.e. (r,theta)
			double requiredOrientation = Math.atan2(x, y);

			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			// turnTo before beginning traversal
			turnTo(requiredOrientation); // with respect to y axis

			// calculate required distance for straight travel
			double desiredDistance = Math.sqrt(x * x + y * y);

			leftMotor.setSpeed(FWD_SPEED);
			rightMotor.setSpeed(FWD_SPEED);
			navigating = true;

			// Path 2
			if (avoidance) {
				leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), true);
				rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), true);

				while (leftMotor.isMoving() || rightMotor.isMoving()) {

					double threshold = 13;

					if (distance < threshold) { // check if distance from US is
												// less than threshold distance
						// make sharp left turn
						leftMotor.setSpeed(ROTATE_SPEED);
						rightMotor.setSpeed(ROTATE_SPEED);
						leftMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(75)), true);
						rightMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(75)), false);
						// use P controller
						avoidance((int) (threshold - distance));
						break; // break out to recalculate
					}
					// set done and return from function if and only if we are
					// within +/- (1,1) of the desired destination
					// otherwise keep going until motors stop and repeat
					else if (Math.abs(xDest - odometer.getX()) <= 1 && Math.abs(yDest - odometer.getY()) <= 1) {
						completed = true;
					}
				}
			}
			// Path 1
			else {
				leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), true);
				rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), false);
				completed = true;
			}
		}	
		completed = false;
		navigating = false;
		leftMotor.stop();
		rightMotor.stop();
	}

	// avoidance method taken from P-Controller
	private static void avoidance(int distError) {

		int diff = calcProp(distError);
		leftMotor.setSpeed(FWD_SPEED - diff);
		rightMotor.setSpeed(FWD_SPEED + diff);

		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setSpeed(FWD_SPEED);
		rightMotor.setSpeed(FWD_SPEED);
		usMotor.setSpeed(ROTATE_SPEED);
		
		//rotate Ultrasonic Sensor motor to face the wall
		usMotor.rotate(-85, false);
		usMotor.stop();
		usMotor.flt();
		
		while (distance <= 18) {
			leftMotor.forward();
			rightMotor.forward();
		}
		// robot goes forward a bit more once US doesn't detect a block
		leftMotor.rotate((int) ((180.0 * 15) / (Math.PI * WHEEL_RADIUS)), true);
		rightMotor.rotate((int) ((180.0 * 15) / (Math.PI * WHEEL_RADIUS)), false);
		// block has been passed
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(85)), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(85)), false);
		
		while (distance <= 18) {
			leftMotor.forward();
			rightMotor.forward();
		}
		
		// robot goes forward a bit more once US doesn't detect a block
		leftMotor.rotate((int) ((180.0 * 24) / (Math.PI * WHEEL_RADIUS)), true);
		rightMotor.rotate((int) ((180.0 * 24) / (Math.PI * WHEEL_RADIUS)), false);
		
		usMotor.rotate(85, false);
		usMotor.stop();
		usMotor.flt();		
		
	}

	private static int calcProp(int diff) { // from slides
		int correction;
		if (diff < 0)
			diff = -diff; // compute absolute value
		correction = (int) (10 * (double) diff); // apply multiplier, ensuring
													// proper truncating.
		if (correction > FWD_SPEED)
			correction = 100; // updated to ensure quicker turning for large
								// anomaly values.
		return correction;
	}

	// turnTo is used to correct orientation
	private static void turnTo(double desiredOrientation) {
		navigating = true;
		double desiredDegrees = (desiredOrientation * 180) / Math.PI;
		double currentOrientation = (odometer.getTheta());
		double theta = desiredDegrees - currentOrientation;
		
		if (theta < -180) {
			//rotate clockwise by (theta+360) rather than counter-clockwise
			leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, (theta+360)), true);
			rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, (theta+360)), false);
		} 
		else if (theta > 180) {
			//rotate counter-clockwise by (theta-360) rather than clockwise
			leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, (theta-360)), true);
			rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, (theta-360)), false);
		}
		else{
			//theta is already within minimal angle range (-180,180)
			leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, (theta)), true);
			rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, (theta)), false);
		}
		navigating = false;
	}

	private static boolean isNavigating() {
		return navigating;

	}

	// taken from square driver Lab-2

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		// fixed to work in radians instead of degrees
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	public void setDistance(int distance) {
		this.distance = distance;

	}

}
