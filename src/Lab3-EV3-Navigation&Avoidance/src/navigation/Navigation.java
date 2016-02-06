package navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Navigation extends Thread {

	// static variables
	private static final int FWD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;

	// static resources
	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	// private static EV3UltrasonicSensor uSensor = new
	// EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));

	private static boolean navigating = false, completed = false, turning = false;
	private static double path[][];

	private static double TRACK;
	private static double WHEEL_RADIUS;

	// class constructor
	public Navigation(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double[][] path, double TRACK, double WHEEL_RADIUS) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.path = path;
		this.TRACK = TRACK;
		this.WHEEL_RADIUS = WHEEL_RADIUS;

		// ensure motors stopped
		leftMotor.stop();
		rightMotor.stop();

		leftMotor.setAcceleration(3000);
		rightMotor.setAcceleration(3000);

	}

	public void run() {
//		for (int i = 0; i < path.length; i++) {
//			travelTo(path[i][0], path[i][1]);
//		}
		travelTo(60, 30);


	}

	private static void travelTo(double xDest, double yDest) {
		// get offset between current location and destination
		double x = xDest - odometer.getX();
		double y = yDest - odometer.getY();

		// atan2 returns the theta angle from converting from rectangular
		// (x,y) coord to polar i.e. (r,theta)
		double requiredOrientation = Math.atan2(x, y);

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		// turn to before beginning traversal
		turnTo(requiredOrientation);

		// calculate required distance
		double desiredDistace = Math.sqrt(x * x + y * y);

		leftMotor.setSpeed(FWD_SPEED);
		rightMotor.setSpeed(FWD_SPEED);

		leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistace), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistace), false);

		// stop motors
		leftMotor.stop();
		rightMotor.stop();


	}

	// used to correct orientation
	private static void turnTo(double desiredOrientation) {

		double currOrientation = odometer.getTheta();

		// determine the angle that we need to turn to
		double turnTo = desiredOrientation - currOrientation;

		if (turnTo < -3.14) {
			turnTo += 6.28;
		} else if (turnTo > 3.14) {
			turnTo -= 6.28;
		}

		// start turn
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, (180.0 * turnTo) / Math.PI), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, (180.0 * turnTo) / Math.PI), false);

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

}
