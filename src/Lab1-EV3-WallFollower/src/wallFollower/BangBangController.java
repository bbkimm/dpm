package wallFollower;
import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
							  int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter-2; //account for different distances for bangbang and pcontroller
		this.bandwidth = bandwidth; //this is the error margin we are allowing for
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		
		int distError=bandCenter-distance;			// Compute error
					
		if (Math.abs(distError) <= bandwidth) {	// Within limits, same speed
			leftMotor.setSpeed(motorHigh);		// Start moving forward maintain same speed on each motor
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();				
		}
			
		else if (distError > 0) {				// Too close to the wall
			leftMotor.setSpeed(motorHigh); //
			rightMotor.setSpeed(motorLow); //reduction in right motor speed moves robot away from wall
			leftMotor.forward(); //start moving
			rightMotor.forward();				
		}
					
		else if (distError < 0) { // too far from wall
			leftMotor.setSpeed(motorLow); //reduction in left motor speed moves robot toward wall. 
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward(); //start moving
			rightMotor.forward();								
		}	
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
