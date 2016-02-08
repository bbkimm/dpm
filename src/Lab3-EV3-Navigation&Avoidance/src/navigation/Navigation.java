package navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {

	private static int distance;

	// static variables
	private static final int FWD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;

	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;

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

		leftMotor.setAcceleration(3000);
		rightMotor.setAcceleration(3000);

		// instantiate

	}

	public void run() {
		// determine which path
		if (avoidance) {
			travelTo(0, 60);
			travelTo(60, 0);
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

			// turn to before beginning traversal
			turnTo(requiredOrientation); // with respect to y axis

			// calculate required distance
			double desiredDistace = Math.sqrt(x * x + y * y);

			leftMotor.setSpeed(FWD_SPEED);
			rightMotor.setSpeed(FWD_SPEED);
			navigating = true;

			if (avoidance) { // path B
				leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistace), true);
				rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistace), true);

				while (leftMotor.isMoving() || rightMotor.isMoving()) {

					double threshold = 40;
					//System.out.println("\n\n\n\n\n\n\n" + distance);
					if (distance < threshold) { // check if distance from US is
												// less than threshold distance
						leftMotor.stop();
						rightMotor.stop();

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

			else {
				leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistace), true);
				rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistace), false);
				completed = true;
			}

		}

		completed = false;
		// stop motors
		leftMotor.stop();
		rightMotor.stop();

	}

	// avoidance method taken from P-Controller
	private static void avoidance(int distError) {
		int diff = calcProp(distError);	
		leftMotor.setSpeed(FWD_SPEED - diff); 
		rightMotor.setSpeed(FWD_SPEED + diff);
		
		while (distance < 40.0) {
			leftMotor.forward();				 
			rightMotor.forward();
		}
		
		leftMotor.setSpeed(FWD_SPEED);
		rightMotor.setSpeed(FWD_SPEED);
		
		leftMotor.rotate((int) ((180.0 * 25) / (Math.PI * WHEEL_RADIUS)), true);
		rightMotor.rotate((int) ((180.0 * 25) / (Math.PI * WHEEL_RADIUS)), false);
		
		

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

	// used to correct orientation
	private static void turnTo(double desiredOrientation) {
		double desiredDegrees = (desiredOrientation * 180) / Math.PI;
		double currentOrientation = (odometer.getTheta());
		if (desiredDegrees - currentOrientation < 0) {
			double theta = desiredDegrees - currentOrientation;
			System.out.println(theta);
			if (Math.abs(theta) < 180) {
				// left turn required
				leftMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), true);
				rightMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), false);
			} else {
				// right turn required
				leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), true);
				rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), false);
			}
		} else {
			double theta = desiredDegrees - currentOrientation;
			if (theta < 180) {
				// right turn required
				leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), true);
				rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), false);
			} else {
				// left turn required
				leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, 360 - theta), true);
				rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, 360 - theta), false);

			}

		}

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
