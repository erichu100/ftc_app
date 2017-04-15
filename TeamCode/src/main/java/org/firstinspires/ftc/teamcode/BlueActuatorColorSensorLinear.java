/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchImpl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.ftc12511.ftc.teamcode.EncoderBasedNavigator;

/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a PushBot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="BlueActuatorColorSensorLinear", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class BlueActuatorColorSensorLinear extends LinearOpMode {

    /* Declare OpMode members. */
    private ElapsedTime runtime = new ElapsedTime();
    byte[] colorCcache;
    public static final int ErrorMargin = 10;
    I2cDevice colorC;
    I2cDeviceSynch colorCReader;
    Servo servo;
    double servoPosition = 0.0;
    DcMotor leftMotor;
    DcMotor rightMotor;
    double power = 0.5;
    boolean timeToStop = false;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        EncoderBasedNavigator navigator;
        colorC = hardwareMap.i2cDevice.get("cc");
        colorCReader = new I2cDeviceSynchImpl(colorC, I2cAddr.create8bit(0x3c), false);
        colorCReader.engage();
        servo = hardwareMap.servo.get("servo");
        servo.setPosition(servoPosition);
        leftMotor = hardwareMap.dcMotor.get("Left_Motor");
        rightMotor = hardwareMap.dcMotor.get("Right_Motor");
        leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        navigator= new EncoderBasedNavigator();
        navigator.Init(this.telemetry, this.hardwareMap);
        waitForStart();
        runtime.reset();
        colorCReader.write8(3,1);

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            //if (!reachedBeacon1)
            //{
            //    reachedBeacon1 = true;
            //}
            navigator.DriveByEncoder(1400, 1400, 5.0);//Drive to corner
            navigator.DriveByEncoder(1400, -1400, 2.5); //Turn 90 degrees

            while (timeToStop == false){
                telemetry.addData("Status", "Run Time: " + runtime.toString());
                telemetry.update();

                leftMotor.setPower(power);
                rightMotor.setPower(power);
                colorCcache = colorCReader.read(0x04, 1);

                //display values
                telemetry.addData("2 #C", colorCcache[0] & 0xFF);

                if (colorCcache[0] > 9 ){
                    leftMotor.setPower(0);
                    rightMotor.setPower(0);
                    double servoPosition=1.0;
                    servo.setPosition(servoPosition);
                    sleep(5000);
                }
                if (colorCcache[0] < 4 ) {
                    leftMotor.setPower(0);
                    rightMotor.setPower(0);
                    double servoPosition=0.0;
                    sleep(5000);
                    servo.setPosition(servoPosition);
                    timeToStop = true;
                    leftMotor.setPower(power);
                    rightMotor.setPower(power);
                }
            }



            idle(); // Always call idle() at the bottom of your while(opModeIsActive()) loop
        }
    }


}