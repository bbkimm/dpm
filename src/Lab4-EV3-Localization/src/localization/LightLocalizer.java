package localization;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocalizer {
	private static final int FWDSPEED = 110; 
	private static final int ROTATION_SPEED = 30;
	private static final long CORRECTION_PERIOD = 5;
	
	private static final double CEN_TO_LIGHT = 8;

	
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
		
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		nav.turnTo(45, true);
		Delay.msDelay(1000);
		leftMotor.setSpeed(FWDSPEED);
		rightMotor.setSpeed(FWDSPEED);
		nav.goForward(15.5);
		Delay.msDelay(500);
		nav.setSpeeds(0, 0);
		
		
		int gridNumber = 0;
		double[] fourAngles = new double[4];
		boolean finished = false;
		
		while(gridNumber<4){
			colorSensor.fetchSample(colorData, 0); //get sample continuously
			nav.setMotorSpeeds(70, -70);
			if(colorData[0] == 13){
				
				fourAngles[gridNumber] = this.odo.getAng();
				gridNumber++;
				Sound.beep();
				try {Thread.sleep(2000);} catch (InterruptedException e) {}
			}
		}
		
		double thetaX = Math.abs(fourAngles[0] - fourAngles[2]);
		double thetaY = Math.abs(fourAngles[1] - fourAngles[3]);
		
		double xOffset = -(CEN_TO_LIGHT)*Math.cos(thetaY/2); 
		double yOffset = -(CEN_TO_LIGHT)*Math.cos(thetaX/2);
		
		odo.setPosition(new double[]{-xOffset, -yOffset,0}, new boolean[] {true,true,false});
		nav.travelTo(0, 0);
		Delay.msDelay(1000);
		nav.setMotorSpeeds(ROTATION_SPEED, ROTATION_SPEED);
		nav.turnTo(0, true);
	}
}
		/*
		// drive to location listed in tutorial
		boolean detected = false;
		leftMotor.setSpeed(FWDSPEED);
		rightMotor.setSpeed(FWDSPEED);
		
		while(!detected) {
			colorSensor.fetchSample(colorData, 0); //get sample continuously
			leftMotor.forward(); //start moving
			rightMotor.forward();
			if(colorData[0] == 13){ //if cross a black line
				//Delay.msDelay(500); //go forward slightly longer
				nav.goForward(5);
				detected = true;
				leftMotor.stop();
				rightMotor.stop();
			}
		}
		detected = false;
		
		nav.turnBy(-90); //turn by 90
		
		while(!detected) {
			colorSensor.fetchSample(colorData, 0); //get sample continuously
			leftMotor.forward(); //start moving
			rightMotor.forward();
			if(colorData[0] == 13){ //if cross a black line
				//Delay.msDelay(500); //go forward slightly longer
				nav.goForward(5);
				detected = true;
				leftMotor.stop();
				rightMotor.stop();
			}
		}
		//should be intersection
		
		
		
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		//determine distance from the wall
		nav.turnTo(180, true); //turn to and stop
		leftMotor.stop();
		rightMotor.stop();
		double xDist = usLocalizer.getFilteredData(); //get xDistance
		
		nav.turnTo(270, true); //turn to and stop
		leftMotor.stop();
		rightMotor.stop();
		double yDist = usLocalizer.getFilteredData(); //get yDistance
		
		
		
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
		double xTheta = (collectedData[3][2] * 180/Math.PI) - (collectedData[1][2] * 180/Math.PI); //4th - 2nd
		double yTheta = (collectedData[2][2] * 180/Math.PI) - (collectedData[0][2] * 180/Math.PI); // 3rd - 1st
		
		//compute new distance
		double distance = 7.3; //TODO: MEASURE DISTANCE FROM CENTER AXEL TO LIGHT SENSOR AT THE BACK
		double x = distance * Math.cos(yTheta/2);
		double y = distance * Math.cos(xTheta/2);
		
		yTheta = yTheta * 180 / Math.PI;
		double thetaOffset = 90 - (collectedData[0][2] * 180/Math.PI) + yTheta/2;
		
		
		//update the odometer
		double[] updatedPos = new double[]{x,y, odo.getAng() + thetaOffset};
		odo.setPosition(updatedPos, new boolean[]{true,true,true});
		
		
	}

	private void storeData(int count) {
		collectedData [count][0] = odo.getX(); //store x
		collectedData [count][1] = odo.getY(); //store y
		collectedData [count][2] = odo.getAng(); //store theta
		
	}

}*/
