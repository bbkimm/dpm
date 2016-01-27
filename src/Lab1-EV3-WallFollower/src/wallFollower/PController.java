package wallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 210, FILTER_OUT = 20; 
	private final int PMULT = 6; //Arbitrary 'Proportion Multiplier' for error adjustment. 
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance; // distance collected from sensor
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
		
		int distError=bandCenter-distance;			// Compute error
		int diff; //init difference var
		
		if (Math.abs(distError) <= bandwidth) {	// Within limits, same speed
			leftMotor.setSpeed(motorStraight);		// Start moving forward at regular speed
			rightMotor.setSpeed(motorStraight);
			leftMotor.forward();
			rightMotor.forward();				
		}
			
		else if (distError > 0) {	// Too close to the wall
			diff = calcProp(distError);// calculate the diff in speed that needs to be applied
			leftMotor.setSpeed(motorStraight + 2 * diff); //applied in the +/- directions to each wheel to ensure tight turns
			rightMotor.setSpeed(motorStraight - diff);
			leftMotor.forward();
			rightMotor.forward(); //initiate wheels turning		
		}
					
		else if (distError < 0) { //too far from the wall
			diff = calcProp(distError); //calculate the diff in speed that needs to be applied
			leftMotor.setSpeed(motorStraight - diff);
			rightMotor.setSpeed(motorStraight + diff);//applied in the +/- directions to each wheel to ensure tight turns
			leftMotor.forward();
			rightMotor.forward(); //initiate wheels turning										
		}
		
		
	}
	
	private int calcProp(int diff) { //from slides
		int correction;
		if(diff < 0) diff = -diff; //compute absolute value
		correction = (int)(PMULT*(double)diff); //apply multiplier, ensuring proper truncating. 
		if(correction > motorStraight) correction = 100; // updated to ensure quicker turning for large anomaly values. 
		return correction;
		
	}

	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
