package wallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 210, FILTER_OUT = 40; //filter_out init = 20;
	private final int PMULT = 6; //Arbitrary 'Proportion Multiplier' for error adjustment
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorStraight);					// Initialize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {
		
		// rudimentary filter - toss out invalid samples corresponding to null signal.
		// (n.b. this was not included in the Bang-bang controller, but easily could have).
		//
		if (distance == 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
		} else if (distance == 255){
			// true 255, therefore set distance to 255
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
		}
		
		// TODO: process a movement based on the us distance passed in (P style)
		int distError=bandCenter-distance;			// Compute error
		int diff;
		
		if (Math.abs(distError) <= bandwidth) {	// Within limits, same speed
			leftMotor.setSpeed(motorStraight);		// Start moving forward
			rightMotor.setSpeed(motorStraight);
			leftMotor.forward();
			rightMotor.forward();				
		}
			
		else if (distError > 0) {	// Too close to the wall
			diff = calcProp(distError);
			leftMotor.setSpeed(motorStraight + diff);
			rightMotor.setSpeed(motorStraight - diff);
			leftMotor.forward();
			rightMotor.forward();		
		}
					
		else if (distError < 0) { //too far
			diff = calcProp(distError);
			leftMotor.setSpeed(motorStraight - diff);
			rightMotor.setSpeed(motorStraight + diff);
			leftMotor.forward();
			rightMotor.forward();								
		}
		
		
	}
	
	private int calcProp(int diff) {
		int correction;
		if(diff < 0) diff = -diff;
		correction = (int)(PMULT*(double)diff);
		if(correction > motorStraight) correction = 100; //50 is max correction
		return correction;
		
	}

	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
