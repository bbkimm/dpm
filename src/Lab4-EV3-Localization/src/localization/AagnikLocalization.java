import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private USLocalizer usl;
	private Navigation nav;
	
	private static final long CORRECTION_PERIOD = 5;
	// array for loading the x, y, theta values when performing light localization
	private double[][] values = new double[4][3];
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData, 
			EV3LargeRegulatedMotor leftmotor, EV3LargeRegulatedMotor rightmotor, 
			USLocalizer usl, Navigation nav) {
		// match variables
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.leftMotor = leftmotor;
		this.rightMotor = rightmotor;
		this.usl = usl;
		this.nav = nav;
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 grid lines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		// turn to 90 degrees and determine distance from wall
		nav.turnTo(270, true);
		leftMotor.stop();
		rightMotor.stop();
		double distanceY = usL.getFilteredData();
	
		// turn to 180 degrees and determine distance from wall
		nav.turnTo(180, true);
		leftMotor.stop();
		rightMotor.stop();
		double distanceX = usL.getFilteredData();
		
		// set new distance
		odo.setPosition(new double[] {-30+distanceX*100, -30+distanceY*100, 0}, new boolean[] {true, true, false});
		Sound.buzz();
		
		// travel to position 0, 0
		nav.travelTo(0,0);
		
		leftMotor.setSpeed(30);
		rightMotor.setSpeed(30);
		
		// begin rotating to collect data
		leftMotor.backward();
		rightMotor.forward();
		
		// begin collecting data and do calculations
		collectData();
		
		
		
	}
	
	private void collectData() {
		
		// counter so the robot stops rotating once it 
		// measures the fourth grid line
		int counter = 0;
		
		// correction period 
		long correctionStart, correctionEnd;
		
		// collect data
		while (true) {
				
			correctionStart = System.currentTimeMillis();

			// get sample from colourSensor
			colorSensor.fetchSample(colorData, 0);
			
			// Black is detected as 13.0
			// if the rover crosses a black line
			// make corrections
			if (colorData[0] == 13.0) {
				Sound.buzz();
				// call method to load data into array
				lineDetected(counter++);
			}			

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
			
			// if fourth line is detected, stop ccollecting data
			if (counter == 4) {
				break;
			}
		}	
		
		// stop the  motor once data collection is performed
		leftMotor.stop();
		rightMotor.stop();
		
		// do calculations with the collected data
		// and update odometer position
		doCalculations();
		
		// travel to (0,0) and set theta to 0
		nav.travelTo(0, 0);
		nav.turnTo(0, true);
		
		// stop the motor once localization complete
		leftMotor.stop();
		rightMotor.stop();
	}
	
	private void lineDetected(int i) {
		
		// load odometer data into values array
		// once line is detected
		values[i][0] = odo.getX();
		values[i][1] = odo.getY();
		values[i][2] = odo.getAng();
		
	}
	
	private void doCalculations() {
		
		// perform calculations using formulas from tutorial slides
		double angleA = (values[0][2]) * Math.PI/180.0;
		double angleB = (values[1][2]) * Math.PI/180.0;
		double angleC = (values[2][2]) * Math.PI/180.0;
		double angleD = (values[3][2]) * Math.PI/180.0;

		double thetaX = angleD - angleB;
		double thetaY = angleC - angleA;
		
		// distance from the light sensor to the centre of rotation
		double d = 13.0;
		
		double x = -d * Math.cos(thetaY/2.0);
		double y = -d * Math.cos(thetaX/2.0);
		
		thetaY = thetaY * 180.0/Math.PI;
		double delTheta = 90 - (angleA - 180.0) + thetaY/2.0;
				
		// update the odometer with corrected values	
		odo.setPosition(new double[] {x, y, odo.getAng() + delTheta}, new boolean[] {true, true, true});
		
	}
	
	


}