package navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class Navigation extends Thread {

	private static int distance;

	// static variables
	private static final int FWD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;

	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static final EV3MediumRegulatedMotor usMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

	private static boolean navigating = false, completed = false, turning = false, avoidance, avoid2 = false;
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
			double desiredDistance = Math.sqrt(x * x + y * y);

			leftMotor.setSpeed(FWD_SPEED);
			rightMotor.setSpeed(FWD_SPEED);
			navigating = true;
			
			// path B
			if (avoidance) { 
			//if(avoid2){
				leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), true);
				rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), true);

				while (leftMotor.isMoving() || rightMotor.isMoving()) {

					double threshold = 20;
					
					//System.out.println("\n\n\n\n\n\n\n" + distance);
					if (distance < threshold) { // check if distance from US is
												// less than threshold distance
						//make sharp left turn
						leftMotor.setSpeed(ROTATE_SPEED);
						rightMotor.setSpeed(ROTATE_SPEED);
						leftMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(70)), true);
						rightMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(70)), false);
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
			
			//Alternative Path B
			else if(avoid2){
			//else if(avoidance){
				/*Sound.beep();
				Sound.beep();*/
				leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), true);
				rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), false);
				double threshold = 10;
				//store x and y destination coordinates of the current travelTo call for a later recursive call
				double xDestTemp = xDest;
				double yDestTemp = yDest;
				
				while (leftMotor.isMoving() || rightMotor.isMoving()) {	
				//while(true){
					if(distance < threshold){
						leftMotor.stop();
						rightMotor.stop();
						
						avoidAlt();
						break;
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
				leftMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), true);
				rightMotor.rotate(convertDistance(WHEEL_RADIUS, desiredDistance), false);
				completed = true;
			}
		}
		completed = false;
		navigating = false;
		// stop motors
		leftMotor.stop();
		rightMotor.stop();
	}
	
	// Simpler hard-coded avoidance alternative with less dynamic approach (such as using P-Controller)
	// ** Not being used for the lab demo. This alternative is here for testing purposes **
	// Note: US Sensor is placed on the right side of the robot
	private static void avoidAlt(){
		Sound.beep();
		double threshold = 10;
		
		//turn left by x degrees
		turnBy(-70);
		//go forward by set distance
		leftMotor.rotate(convertDistance(WHEEL_RADIUS,23), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS,23), false);
		//turn right by x degrees to face "forward"
		turnBy(70);
		//call avoidAlt recursively if block is within sight of US
		if(distance<threshold){
			avoidAlt();
		}
		else if(distance>threshold){
			//go forward by set distance to get past obstacle
			leftMotor.rotate(convertDistance(WHEEL_RADIUS,25), true);
			rightMotor.rotate(convertDistance(WHEEL_RADIUS,25), false);
			
			turnBy(70);
			leftMotor.rotate(convertDistance(WHEEL_RADIUS,23), true);
			rightMotor.rotate(convertDistance(WHEEL_RADIUS,23), false);
			turnBy(-70);
		}
	}
	// rotate robot by a set angle in degrees, used for avoidAlt
	private static void turnBy (double angle){
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, angle), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, angle), false);	
	}
	
	
	// avoidance method taken from P-Controller
	private static void avoidance(int distError) {
		
		int diff = calcProp(distError);	
		leftMotor.setSpeed(FWD_SPEED - diff); 
		rightMotor.setSpeed(FWD_SPEED + diff);
		
		while (distance < 18.0) { //move away from the wall!
			leftMotor.forward();				 
			rightMotor.forward();
		}
		
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setSpeed(FWD_SPEED);
		rightMotor.setSpeed(FWD_SPEED);
		usMotor.setSpeed(ROTATE_SPEED);
		usMotor.rotate(-85, false);
		usMotor.stop();
		usMotor.flt();
		while(distance <= 18){
			leftMotor.forward();
			rightMotor.forward();
		}
		// robot goes forward a bit more once US doesn't detect a block
		leftMotor.rotate((int) ((180.0 * 20) / (Math.PI * WHEEL_RADIUS)), true);
		rightMotor.rotate((int) ((180.0 * 20) / (Math.PI * WHEEL_RADIUS)), false);
		//block has been passed
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(70)), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(70)), false);
		
		while(distance <= 10){
			leftMotor.forward();
			rightMotor.forward();
		}
		
		usMotor.rotate(85, false);
		usMotor.stop();
		usMotor.flt();
		
		leftMotor.rotate((int) ((180.0 * 20) / (Math.PI * WHEEL_RADIUS)), true);
		rightMotor.rotate((int) ((180.0 * 20) / (Math.PI * WHEEL_RADIUS)), false);
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
		navigating = true;
		double desiredDegrees = (desiredOrientation * 180) / Math.PI;
		double currentOrientation = (odometer.getTheta());
		if (desiredDegrees - currentOrientation < 0) {
			double theta = desiredDegrees - currentOrientation;
			System.out.println(theta);
			if (Math.abs(theta) < 180) {
				// left turn required
				leftMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), true);
				rightMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), false);
			} 
			else {
				// right turn required
				leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), true);
				rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.abs(theta)), false);
			}
		} 
		else {
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
