/*
 * Copyright (c) 2018 Titan Robotics Club (http://www.titanrobotics.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package team492;

import frclib.FrcJoystick;
import hallib.HalDashboard;
import trclib.TrcRobot;

public class FrcTeleOp implements TrcRobot.RobotMode
{
    private enum DriveMode
    {
        MECANUM_MODE, ARCADE_MODE, TANK_MODE
    } // enum DriveMode

    protected Robot robot;

    private boolean slowDriveOverride = false;
    private DriveMode driveMode = DriveMode.MECANUM_MODE;
    private boolean driveInverted = false;
    private boolean gyroAssist = false;

    public FrcTeleOp(Robot robot)
    {
        this.robot = robot;
    } // FrcTeleOp

    //
    // Implements TrcRobot.RunMode interface.
    //

    @Override
    public void startMode()
    {
        HalDashboard.getInstance().clearDisplay();
        robot.setVisionEnabled(true);

        robot.driveBase.resetPosition();
        robot.targetHeading = 0.0;

        robot.encoderXPidCtrl.setOutputRange(-1.0, 1.0);
        robot.encoderYPidCtrl.setOutputRange(-1.0, 1.0);
        robot.gyroTurnPidCtrl.setOutputRange(-1.0, 1.0);
        //robot.sonarDrivePidCtrl.setOutputRange(-1.0, 1.0);
        //
        // Configure joysticks.
        //
        robot.leftDriveStick.setButtonHandler(this::leftDriveStickButtonEvent);
        robot.leftDriveStick.setYInverted(true);

        robot.rightDriveStick.setButtonHandler(this::rightDriveStickButtonEvent);
        robot.rightDriveStick.setYInverted(true);

        robot.operatorStick.setButtonHandler(this::operatorStickButtonEvent);
        robot.operatorStick.setYInverted(false);
    } // startMode

    @Override
    public void stopMode()
    {
        robot.setVisionEnabled(false);
    } // stopMode

    @Override
    public void runPeriodic(double elapsedTime)
    {
        if (!robot.cmdAutoCubePickup.isEnabled())
        {
            //
            // DriveBase operation.
            //
            switch (driveMode)
            {
                case TANK_MODE:
                    double leftPower = robot.leftDriveStick.getYWithDeadband(true);
                    double rightPower = robot.rightDriveStick.getYWithDeadband(true);
                    if (slowDriveOverride)
                    {
                        leftPower /= RobotInfo.DRIVE_SLOW_YSCALE;
                        rightPower /= RobotInfo.DRIVE_SLOW_YSCALE;
                    }
                    robot.driveBase.tankDrive(leftPower, rightPower, driveInverted);
                    break;

                case ARCADE_MODE:
                    double drivePower = robot.rightDriveStick.getYWithDeadband(true);
                    double turnPower = robot.rightDriveStick.getTwistWithDeadband(true);
                    if (slowDriveOverride)
                    {
                        drivePower /= RobotInfo.DRIVE_SLOW_YSCALE;
                        turnPower /= RobotInfo.DRIVE_SLOW_TURNSCALE;
                    }
                    robot.driveBase.arcadeDrive(drivePower, turnPower, driveInverted);
                    break;

                default:
                case MECANUM_MODE:
                    double x = robot.leftDriveStick.getXWithDeadband(true);
                    double y = robot.rightDriveStick.getYWithDeadband(true);
                    double rot = robot.rightDriveStick.getTwistWithDeadband(true);
                    if (slowDriveOverride)
                    {
                        x /= RobotInfo.DRIVE_SLOW_XSCALE;
                        y /= RobotInfo.DRIVE_SLOW_YSCALE;
                        rot /= RobotInfo.DRIVE_SLOW_TURNSCALE;
                    }
                    double xForceOz = x * RobotInfo.MAX_WHEEL_FORCE_OZ;
                    double yForceOz = y * RobotInfo.MAX_WHEEL_FORCE_OZ;
                    robot.driveBase.mecanumDrive_Cartesian(x, y, rot, driveInverted);
                    HalDashboard.putNumber("xForceOz", xForceOz);
                    HalDashboard.putNumber("yForceOz", yForceOz);
                    break;
            }

            double elevatorPower = robot.operatorStick.getYWithDeadband(true);
            robot.elevator.setPower(elevatorPower); // Pull joystick back -> move elevator up
        }

        robot.updateDashboard();
        robot.announceSafety();
    } // runPeriodic

    @Override
    public void runContinuous(double elapsedTime)
    {
        if (robot.cmdAutoCubePickup.isEnabled())
        {
            robot.tracer.tracePrintf("Activated: %b", robot.cmdAutoCubePickup.isEnabled());
            robot.cmdAutoCubePickup.cmdPeriodic(elapsedTime);
        }
    } // runContinuous

    //
    // Implements TrcJoystick.ButtonHandler.
    //

    public void leftDriveStickButtonEvent(int button, boolean pressed)
    {
        robot.dashboard.displayPrintf(8, " LeftDriveStick: button=0x%04x %s", button, pressed? "pressed": "released");

        switch (button)
        {
            case FrcJoystick.LOGITECH_TRIGGER:
                slowDriveOverride = pressed;
                break;

            case FrcJoystick.LOGITECH_BUTTON2:
                if (pressed && !robot.cmdAutoCubePickup.isEnabled())
                {
                    robot.cmdAutoCubePickup.start();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON3:
                if (pressed && robot.cmdAutoCubePickup.isEnabled())
                {
                    robot.cmdAutoCubePickup.stop();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON4:
                break;

            case FrcJoystick.LOGITECH_BUTTON5:
                break;

            case FrcJoystick.LOGITECH_BUTTON6:
                break;

            case FrcJoystick.LOGITECH_BUTTON7:
                break;

            case FrcJoystick.LOGITECH_BUTTON8:
                break;

            case FrcJoystick.LOGITECH_BUTTON9:
                break;

            case FrcJoystick.LOGITECH_BUTTON10:
                break;

            case FrcJoystick.LOGITECH_BUTTON11:
                break;

            case FrcJoystick.LOGITECH_BUTTON12:
                break;
        }
    } // leftDriveStickButtonEvent

    public void rightDriveStickButtonEvent(int button, boolean pressed)
    {
        robot.dashboard.displayPrintf(8, "RightDriveStick: button=0x%04x %s", button, pressed? "pressed": "released");

        switch (button)
        {
            case FrcJoystick.SIDEWINDER_TRIGGER:
//                if (pressed)
//                {
//                    driveInverted = !driveInverted;
//                }
                break;

            case FrcJoystick.SIDEWINDER_BUTTON2:
                break;

            case FrcJoystick.SIDEWINDER_BUTTON3:
            	if(pressed)
            	{
            		robot.leftFlipper.extendThenRetract(RobotInfo.FLIPPER_DELAY);
            	}
                break;

            case FrcJoystick.SIDEWINDER_BUTTON4:
            	if(pressed)
            	{
            		robot.rightFlipper.extendThenRetract(RobotInfo.FLIPPER_DELAY);
            	}
                break;

            case FrcJoystick.SIDEWINDER_BUTTON5:
                if (pressed)
                {
                    gyroAssist = !gyroAssist;
                    if (gyroAssist)
                    {
                        robot.driveBase.enableGyroAssist(
                            RobotInfo.DRIVE_MAX_ROTATION_RATE, RobotInfo.DRIVE_GYRO_ASSIST_KP);
                    }
                    else
                    {
                        robot.driveBase.disableGyroAssist();
                    }
                }
                break;

            case FrcJoystick.SIDEWINDER_BUTTON6:
                break;

            case FrcJoystick.SIDEWINDER_BUTTON7:
                break;

            case FrcJoystick.SIDEWINDER_BUTTON8:
                break;

            case FrcJoystick.SIDEWINDER_BUTTON9:
                break;
        }
    } // rightDriveStickButtonEvent

    public void operatorStickButtonEvent(int button, boolean pressed)
    {
        robot.dashboard.displayPrintf(8, "  OperatorStick: button=0x%04x %s", button, pressed? "pressed": "released");

        switch (button)
        {
            case FrcJoystick.LOGITECH_TRIGGER:
                if (pressed)
                {
                    robot.cubePickup.grabCube(RobotInfo.PICKUP_TELEOP_POWER, null);
                }
                else
                {
                    robot.cubePickup.stopPickup();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON2:
                robot.elevator.setManualOverride(pressed);
//                if (pressed)
//                {
//                    robot.cubePickup.grabCube(0.5);
//                }
//                else
//                {
//                    robot.cubePickup.grabCube(0.0);
//                }
                break;

            case FrcJoystick.LOGITECH_BUTTON3:
                if (pressed)
                {
                    robot.cubePickup.dropCube(RobotInfo.PICKUP_TELEOP_POWER);
                }
                else
                {
                    robot.cubePickup.stopPickup();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON4:
                //
                // Cube Claw open.
                //
                if (pressed)
                {
                    robot.cubePickup.openClaw();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON5:
                //
                // Cube Claw close.
                //
                if (pressed)
                {
                    robot.cubePickup.closeClaw();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON6:
                //
                // Cube Deployer down.
                //
                if (pressed)
                {
                    robot.cubePickup.raisePickup();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON7:
                //
                // Cube Deployer up.
                //
                if (pressed)
                {
                    robot.cubePickup.deployPickup();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON8:
                if (pressed)
                {
                    robot.leftFlipper.extend();
                }
                else
                {
                    robot.leftFlipper.retract();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON9:
                if (pressed)
                {
                    robot.rightFlipper.extend();
                }
                else
                {
                    robot.rightFlipper.retract();
                }
                break;

            case FrcJoystick.LOGITECH_BUTTON10:
                robot.winch.setPower(pressed? RobotInfo.WINCH_TELEOP_POWER: 0.0);
                break;

            case FrcJoystick.LOGITECH_BUTTON11:
                robot.winch.setPower(pressed? -RobotInfo.WINCH_TELEOP_POWER: 0.0);
                break;

            case FrcJoystick.LOGITECH_BUTTON12:
                break;
        }
    } // operatorStickButtonEvent

} // class FrcTeleOp
