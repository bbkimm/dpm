/*
 * Group 51
 * Brian Kim-Lim (260636766)
 * Jason Dias (260617554)
 * 
 * Lab 5 - Ballistics
 */

package ballistic;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.utility.Delay;
import lejos.hardware.Button;


public class Lab5 {

	private static final Port usPort = LocalEV3.get().getPort("A");
	private static final EV3LargeRegulatedMotor launchMotorOne = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor launchMotorTwo = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	public static void main(String[] args) {
		//setup
		
		
		while (true) {
			
			Button.waitForAnyPress(); // wait for button to release
			
			//High speed and acceleration is needed to have enough power to launch the ball at ~120cm away from
			//the objective tile
			launchMotorOne.setSpeed(5500);
			launchMotorOne.setAcceleration(100000);
			launchMotorTwo.setSpeed(5500);
			launchMotorTwo.setAcceleration(100000);
			
			//From our starting angle position, rotate 80 degrees to reach roughly the optimal launch angle
			launchMotorOne.rotate(-80,true);
			launchMotorTwo.rotate(-80,false);
			
			//Stop motors to help avoid floating motors and an oscillating catapult arm
			launchMotorOne.stop();
			launchMotorTwo.stop();
			
			//Lowers the arm for re-use
			launchMotorOne.setSpeed(50);
			launchMotorTwo.setSpeed(50);
			launchMotorOne.rotate(80,true);
			launchMotorTwo.rotate(80,false);
			
			
		}
		

	}
}
