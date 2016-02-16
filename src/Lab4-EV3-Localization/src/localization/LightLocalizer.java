package localization;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocalizer {
	private static final int FWDSPEED = 110; 
	private static final int ROTATION_SPEED = 30;
	private static final long CORRECTION_PERIOD = 5;

	
	private Odometer odo;
	private SampleProvider colorSensor;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private USLocalizer usLocalizer;
	private Navigation nav;
	private float[] colorData;	
	
	private double[][] collectedData = new double[4][3];
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData,
			EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
			USLocalizer usLocalizer, Navigation nav) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usLocalizer = usLocalizer;
		this.nav = nav;
		
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		boolean detected = false;
		leftMotor.setSpeed(FWDSPEED);
		rightMotor.setSpeed(FWDSPEED);
		
		
		
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		//determine distance from the wall
		nav.turnTo(270, true); //turn to and stop
		leftMotor.stop();
		rightMotor.stop();
		double yDist = usLocalizer.getFilteredData(); //get yDistance
		
		nav.turnTo(180, true); //turn to and stop
		leftMotor.stop();
		rightMotor.stop();
		double xDist = usLocalizer.getFilteredData(); //get xDistance
		
		//calculate and set new distances
		//determine position in 1x1 block
		xDist = 100*xDist - 30;
		yDist = 100*yDist - 30;
		odo.setPosition(new double[]{xDist, yDist}, new boolean[]{true,true,false});
		
		
		//odo position now set
		nav.travelTo(0, 0);
		//wait for return
		
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		
		//initiate turn until correct() task completed 
		leftMotor.backward();
		rightMotor.forward();
		
		correct();
		
		
	}
	
	private void correct(){
		int count = 0;
		while (count < 4) {
			
			long correctionStart = System.currentTimeMillis();

			colorSensor.fetchSample(colorData, 0);
			
			//line detected at 13
			if(colorData[0] == 13.0) {
				Sound.beep();
				storeData(count++);
				
			}

			// this ensure the odometry correction occurs only once every period
			long correctionEnd = System.currentTimeMillis();
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
		}
		
		//stop moving
		leftMotor.stop();
		rightMotor.stop();
		
		calculateCorrection();
		
		//calculation complate, now travel to this improved position
		nav.travelTo(0, 0);
		nav.turnTo(0,true);
		
		leftMotor.stop();
		rightMotor.stop();
	}

	private void calculateCorrection() {

		
		//compute new angles
		double xTheta = (collectedData[3][2] * Math.PI/180) - (collectedData[1][2] * Math.PI/180); //4th - 2nd
		double yTheta = (collectedData[2][2] * Math.PI/180) - (collectedData[0][2] * Math.PI/180); // 3rd - 1st
		
		//compute new distance
		double distance = 7.0; //TODO: MEASURE DISTANCE FROM CENTER AXEL TO LIGHT SENSOR AT THE BACK
		double x = distance * Math.cos(yTheta/2);
		double y = distance * Math.cos(xTheta/2);
		
		yTheta = yTheta * 180 / Math.PI;
		double thetaOffset = 90 - (collectedData[0][2] * 180/Math.PI  - 180) + yTheta/2;
		
		
		//update the odometer
		double[] updatedPos = new double[]{x,y, odo.getAng() + thetaOffset + 20};
		odo.setPosition(updatedPos, new boolean[]{true,true,true});
		
		
	}

	private void storeData(int count) {
		collectedData [count][0] = odo.getX(); //store x
		collectedData [count][1] = odo.getY(); //store y
		collectedData [count][2] = odo.getAng(); //store theta
		
	}

}
