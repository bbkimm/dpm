package localization;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	
	public static final int ROTATION_SPEED = 40;
	
	// Threshold for distance of wall detection in (cm)
	// Threshold error (noise margin) in (cm)
	private static final double THRESHOLD = 30;
	private static final double THRESH_ERR = 3;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
	}
	
	public void doLocalization(Navigation nav) {
		double [] pos = new double [3];
		double angleA, angleB, thetaD;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			while(getFilteredData() < (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}
			// Delay to allow for filtering of erroneous sensor values
			Delay.msDelay(1000);
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}
			
			// Stop the robot's rotation
			nav.setSpeeds(0, 0);
			
			// angleB is for left wall
			angleB = this.odo.getAng();
			Sound.beep();
			
			// Delay to allow for proper settling of robot
			Delay.msDelay(1000);
			
			
			
			// switch direction and wait until it sees no wall
			while(getFilteredData() < (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}
			Delay.msDelay(1000);
			nav.turnBy(15);
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}
			nav.setSpeeds(0, 0);
			
			// angleA is for back wall
			angleA = this.odo.getAng();
			Sound.beep();
			
			Delay.msDelay(1000);
		
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'

			if(angleA < angleB){
				thetaD = 45 - (angleA + angleB)/2;
			}
			else{
				thetaD = 225 - (angleA + angleB)/2;
			}
			
			// update the odometer position (example to follow:)
			// Updates odometer's orientation, but not x-y coordinates
			double newTheta = this.odo.getAng() + thetaD;
			this.odo.setPosition(new double [] {0.0, 0.0, newTheta}, new boolean [] {false, false, true});
			
			// Turn to 0 degrees
			nav.setMotorSpeeds(100, 100);
			nav.turnTo(0, true);
		} 
		
		else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			// rotate the robot until it sees a wall
			while(getFilteredData() > (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}
			// Delay to allow for filtering of erroneous sensor values
			Delay.msDelay(1000);
			
			// keep rotating until the robot sees no wall, then latch the angle
			while(getFilteredData() < (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}
			// Stop the robot's rotation
			nav.setSpeeds(0, 0);
			
			// angleA is for back wall
			angleA = this.odo.getAng();
			Sound.beep();
			
			// Delay to allow for proper settling of robot
			Delay.msDelay(1000);
			
			
			
			// switch direction and wait until it sees a wall
			while(getFilteredData() > (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}
			Delay.msDelay(1000);
			
			// keep rotating until the robot sees no wall, then latch the angle
			while(getFilteredData() < (THRESHOLD + THRESH_ERR)){
				nav.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}
			
			nav.setSpeeds(0, 0);
			
			// angleB is for left wall
			angleB = this.odo.getAng();
			Sound.beep();
			
			Delay.msDelay(1000);
		
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			if(angleA < angleB){
				thetaD = 45 - (angleA + angleB)/2;
			}
			else{
				thetaD = 225 - (angleA + angleB)/2;
			}
			
			// update the odometer position (example to follow:)
			// Updates odometer's orientation, but not x-y coordinates
			double newTheta = this.odo.getAng() + thetaD;
			this.odo.setPosition(new double [] {0.0, 0.0, newTheta}, new boolean [] {false, false, true});
			
			// Turn to 0 degrees
			nav.setMotorSpeeds(100, 100);
			nav.turnTo(0, true);
		}
	}
	
	public float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
		
		if(distance > .50)
			distance = 255;
		
		final TextLCD LCD = LocalEV3.get().getTextLCD();;
		LCD.drawString("US: "+distance, 0, 4);
		
		return distance;
	}

}
