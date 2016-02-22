package ballistic;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

import lejos.hardware.Button;

//
// Lab 1:  EV3 Wall Following robot
//
// This is the main class for the wall follower.

public class Lab5 {

	private static final Port usPort = LocalEV3.get().getPort("A");
	private static final EV3LargeRegulatedMotor launchMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	public static void main(String[] args) {
		//setup
		

		while (true) {
			
			Button.waitForAnyPress(); // wait for button to release
			launchMotor.setSpeed(2000);
			launchMotor.setAcceleration(20000);
			
			launchMotor.rotate(-65);
			launchMotor.setSpeed(50);
			launchMotor.rotate(65);
			
			
		}

	}
}
