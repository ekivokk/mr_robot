package fr.lib;
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  ControlGpioExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2016 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.math.BigDecimal;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * This example code demonstrates how to perform simple state control of a GPIO
 * pin on the Raspberry Pi.
 *
 * @author Robert Savage
 */

public class Movement implements Runnable {
	final GpioPinDigitalOutput pin11;
	final GpioPinDigitalOutput pin12;
	final GpioPinDigitalOutput pin13;
	final GpioPinDigitalOutput pin14;
	// Create custom PCA9685 GPIO provider
	PCA9685GpioProvider gpioProvider = null;

	private static double x = 0.0d;
	private static double y = 0.0d;
	private int speed = ((int) Math.abs(x) * 10);
	private int position = 0;
	private static final int SERVO_DURATION_MIN = 900;
	private static final int SERVO_DURATION_NEUTRAL = 1500;
	private static final int SERVO_DURATION_MAX = 2100;

	public Movement() {
		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #11 #12 #13 #14 as an output pin and turn off
		pin11 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, "MotorA_0", PinState.LOW);
		pin12 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "MotorA_1", PinState.LOW);
		pin13 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, "MotorB_0", PinState.LOW);
		pin14 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14, "MotorB_1", PinState.LOW);

		// This would theoretically lead into a resolution of 5 microseconds per
		// step:
		// 4096 Steps (12 Bit)
		// T = 4096 * 0.000005s = 0.02048s
		// f = 1 / T = 48.828125
		BigDecimal frequency = new BigDecimal("48.828");
		// Correction factor: actualFreq / targetFreq
		// e.g. measured actual frequency is: 51.69 Hz
		// Calculate correction factor: 51.65 / 48.828 = 1.0578
		// --> To measure actual frequency set frequency without correction
		// factor(or set to 1)
		BigDecimal frequencyCorrectionFactor = new BigDecimal("1.0578");

		try {
			I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
			gpioProvider = new PCA9685GpioProvider(bus, 0x40, frequency, frequencyCorrectionFactor);
		} catch (UnsupportedBusNumberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Define outputs in use for this example
		GpioPinPwmOutput[] myOutputs = provisionPwmOutputs(gpioProvider);
		// Reset outputs
		gpioProvider.reset();

	}

	private static GpioPinPwmOutput[] provisionPwmOutputs(final PCA9685GpioProvider gpioProvider) {
		GpioController gpio = GpioFactory.getInstance();
		GpioPinPwmOutput myOutputs[] = { gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_00, "Servo Steering"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_01, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_02, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_03, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_04, "Car Speed"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_05, "Car Speed"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_06, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_07, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_08, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_09, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_10, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_11, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_12, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_13, "not used"),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_14, "Servo Cam Horizontal "),
				gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_15, "Servo Cam Vertical") };
		return myOutputs;
	}

	public static double getX() {
		return x;
	}

	public static void setX(double x) {
		Movement.x = x;
	}

	public static double getY() {
		return y;
	}

	public static void setY(double y) {
		Movement.y = y;
	}

	@Override
	public void run() {

		while (true) {
			if (x > 1.0) {
				pin11.high();
				pin12.low();
				pin13.high();
				pin14.low();
				gpioProvider.setPwm(PCA9685Pin.PWM_04, speed);
				gpioProvider.setPwm(PCA9685Pin.PWM_04, speed);
				if (y > 1.0) {
					// avant droite
					if ((SERVO_DURATION_NEUTRAL + (2 * (int) y)) < SERVO_DURATION_MAX) {
						position = SERVO_DURATION_NEUTRAL + (2 * (int) y);
						gpioProvider.setPwm(PCA9685Pin.PWM_00, position);
					} else {
						gpioProvider.setPwm(PCA9685Pin.PWM_00, SERVO_DURATION_MAX);
					}
				} else if (y < -1.0) {
					// avant gauche
					if ((SERVO_DURATION_NEUTRAL + (2 * (int) y)) > SERVO_DURATION_MIN) {
						position = SERVO_DURATION_NEUTRAL + (2 * (int) y);
						gpioProvider.setPwm(PCA9685Pin.PWM_00, position);
					} else {
						gpioProvider.setPwm(PCA9685Pin.PWM_00, SERVO_DURATION_MIN);
					}
				} else {
					// avant
					gpioProvider.setPwm(PCA9685Pin.PWM_00, SERVO_DURATION_NEUTRAL);
				}

			} else if (x < -1.0) {
				pin11.low();
				pin12.high();
				pin13.low();
				pin14.high();
				gpioProvider.setPwm(PCA9685Pin.PWM_04, speed);
				gpioProvider.setPwm(PCA9685Pin.PWM_04, speed);

				if (y > 1.0) {
					// arriere droite
					if ((SERVO_DURATION_NEUTRAL + (2 * (int) y)) < SERVO_DURATION_MAX) {
						position = SERVO_DURATION_NEUTRAL + (2 * (int) y);
						gpioProvider.setPwm(PCA9685Pin.PWM_00, position);
					} else {
						gpioProvider.setPwm(PCA9685Pin.PWM_00, SERVO_DURATION_MAX);
					}
				} else if (y < -1.0) {
					// arriere gauche
					if ((SERVO_DURATION_NEUTRAL + (2 * (int) y)) > SERVO_DURATION_MIN) {
						position = SERVO_DURATION_NEUTRAL + (2 * (int) y);
						gpioProvider.setPwm(PCA9685Pin.PWM_00, position);
					} else {
						gpioProvider.setPwm(PCA9685Pin.PWM_00, SERVO_DURATION_MIN);
					}
				} else {
					// arriere
					gpioProvider.setPwm(PCA9685Pin.PWM_00, SERVO_DURATION_NEUTRAL);
				}

			} else {
				// arret
				pin11.low();
				pin12.low();
				pin13.low();
				pin14.low();
				gpioProvider.setPwm(PCA9685Pin.PWM_04, 0);
				gpioProvider.setPwm(PCA9685Pin.PWM_04, 0);
				gpioProvider.setPwm(PCA9685Pin.PWM_00, SERVO_DURATION_NEUTRAL);
			}
		}
	}

}
