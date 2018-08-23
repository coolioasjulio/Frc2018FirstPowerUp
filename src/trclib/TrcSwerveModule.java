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

package trclib;

/**
 * This class implements a platform independent Swerve Drive module. A Swerve Drive module consists of a drive motor
 * and a steer motor. The steer motor is a PID controlled motor that allows a steering angle to be set and held. It
 * implements the TrcMotorController interface so that it can be used in TrcDriveBase.
 */
public class TrcSwerveModule implements TrcMotorController
{
    private static final String moduleName = "TrcSwerveModule";
    private static final boolean debugEnabled = false;
    private static final boolean tracingEnabled = false;
    private static final boolean useGlobalTracer = false;
    private static final TrcDbgTrace.TraceLevel traceLevel = TrcDbgTrace.TraceLevel.API;
    private static final TrcDbgTrace.MsgLevel msgLevel = TrcDbgTrace.MsgLevel.INFO;
    private TrcDbgTrace dbgTrace = null;

    private final String instanceName;
    public final TrcMotorController driveMotor;
    public final TrcPidMotor steerMotor;

    /**
     * Constructor: Create an instance of the object.
     *
     * @param instanceName specifies the instance name.
     * @param driveMotor specifies the drive motor.
     * @param steerMotor specifies the steering motor.
     */
    public TrcSwerveModule(String instanceName, TrcMotorController driveMotor, TrcPidMotor steerMotor)
    {
        if (debugEnabled)
        {
            dbgTrace = useGlobalTracer?
                TrcDbgTrace.getGlobalTracer():
                    new TrcDbgTrace(moduleName, tracingEnabled, traceLevel, msgLevel);
        }

        this.instanceName = instanceName;
        this.driveMotor = driveMotor;
        this.steerMotor = steerMotor;
    }   //TrcSwerveModule

    /**
     * This method returns the instance name.
     *
     * @return instance name.
     */
    public String toString()
    {
        return instanceName;
    }   //toString

    /**
     * Reset the encoder of the drive and steer motors.
     *
     * @param hardware specifies true for resetting hardware position, false for resetting software position.
     * @param resetSteerPosition specifies true to also reset the steer motor encoder, false otherwise.
     */
    public void resetPosition(boolean hardware, boolean resetSteerPosition)
    {
        final String funcName = "resetPosition";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API,
                "hardware=%s,resetSteerPosition=%s", hardware, resetSteerPosition);
        }

        driveMotor.resetPosition(hardware);

        if(resetSteerPosition)
        {
            steerMotor.getMotor().resetPosition(hardware);
        }

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }
    }   //resetPosition

    /**
     * This method sets the steer angle. The steer motor will hold this angle.
     *
     * @param angle specifies the angle in degrees to set the steer motor to, in the range [0,360).
     * @param hold specifies true to hold the angle.
     */
    public void setSteerAngle(double angle, boolean hold)
    {
        final String funcName = "setSteerAngle";

        angle = TrcUtil.modulo(angle, 360);

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API, "angle=%f", angle);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }

        steerMotor.setTarget(angle, hold);
    }   //setSteerAngle

    /**
     * This method sets the steer angle. The steer motor will hold this angle.
     *
     * @param angle specifies the angle in degrees to set the steer motor to, in the range [0,360).
     */
    public void setSteerAngle(double angle)
    {
        setSteerAngle(angle, true);
    }   //setSteerAngle

    /**
     * The current angle of the turn motor. This is not necessarily the target angle.
     *
     * @return The angle of the turn motor, in degrees, in the range [0,360).
     */
    public double getSteerAngle()
    {
        final String funcName = "getSteerAngle";

        double angle = TrcUtil.modulo(steerMotor.getPosition(), 360.0);

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API, "=%f", angle);
        }

        return angle;
    }   //getSteerAngle

    //
    // Implements TrcMotorController interface.
    //

    /**
     * This method returns the state of the motor controller direction.
     *
     * @return true if the motor direction is inverted, false otherwise.
     */
    @Override
    public boolean getInverted()
    {
        final String funcName = "getInverted";
        boolean inverted = driveMotor.getInverted();

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API, "=%s", inverted);
        }

        return inverted;
    }   //getInverted

    /**
     * This method returns the motor position by reading the position sensor. The position sensor can be an encoder
     * or a potentiometer.
     *
     * @return current motor position.
     */
    @Override
    public double getPosition()
    {
        final String funcName = "getPosition";
        double position = driveMotor.getPosition();

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API, "=%f", position);
        }

        return position;
    }   //getPosition

    /**
     * This method gets the last set power.
     *
     * @return the last setPower value.
     */
    @Override
    public double getPower()
    {
        final String funcName = "getPower";
        double power = driveMotor.getPower();

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API, "=%f", power);
        }

        return power;
    }   //getPower

    /**
     * This method returns the speed of the motor rotation.
     *
     * @return motor rotation speed.
     */
    @Override
    public double getSpeed()
    {
        final String funcName = "getSpeed";
        double speed = driveMotor.getSpeed();

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API, "=%f", speed);
        }

        return speed;
    }   //getSpeed

    /**
     * This method returns the state of the lower limit switch.
     *
     * @return true if lower limit switch is active, false otherwise.
     */
    @Override
    public boolean isLowerLimitSwitchActive()
    {
        throw new UnsupportedOperationException("Drive wheel does not have limit switches.");
    }   //isLowerLimitSwitchActive

    /**
     * This method returns the state of the upper limit switch.
     *
     * @return true if upper limit switch is active, false otherwise.
     */
    @Override
    public boolean isUpperLimitSwitchActive()
    {
        throw new UnsupportedOperationException("Drive wheel does not have limit switches.");
    }   //isUpperLimitSwitchActive

    /**
     * This method resets the motor position sensor, typically an encoder.
     *
     * @param hardware specifies true for resetting hardware position, false for resetting software position.
     */
    @Override
    public void resetPosition(boolean hardware)
    {
        resetPosition(hardware, false);
    }   //resetPosition

    /**
     * This method sets the motor output value. The value can be power or velocity percentage depending on whether
     * the motor controller is in power mode or velocity mode.
     *
     * @param value specifies the percentage power or velocity (range -1.0 to 1.0) to be set.
     */
    @Override
    public void set(double value)
    {
        final String funcName = "set";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API, "value=%f", value);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }

        driveMotor.set(value);
    }   //set

    /**
     * This method enables/disables motor brake mode. In motor brake mode, set power to 0 would stop the motor very
     * abruptly by shorting the motor wires together using the generated back EMF to stop the motor. When brakMode
     * is false (i.e. float/coast mode), the motor wires are just disconnected from the motor controller so the motor
     * will stop gradually.
     *
     * @param enabled specifies true to enable brake mode, false otherwise.
     */
    @Override
    public void setBrakeModeEnabled(boolean enabled)
    {
        final String funcName = "setBrakeModeEnabled";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API, "enabled=%s", enabled);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }

        driveMotor.setBrakeModeEnabled(enabled);
    }   //setBrakeModeEnabled

    /**
     * This method inverts the motor direction.
     *
     * @param inverted specifies true to invert motor direction, false otherwise.
     */
    @Override
    public void setInverted(boolean inverted)
    {
        final String funcName = "setInverted";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API, "inverted=%s", inverted);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }

        driveMotor.setInverted(inverted);
    }   //setInverted

    /**
     * This method inverts the position sensor direction. This may be rare but there are scenarios where the motor
     * encoder may be mounted somewhere in the power train that it rotates opposite to the motor rotation. This will
     * cause the encoder reading to go down when the motor is receiving positive power. This method can correct this
     * situation.
     *
     * @param inverted specifies true to invert position sensor direction, false otherwise.
     */
    @Override
    public void setPositionSensorInverted(boolean inverted)
    {
        final String funcName = "setPositionSensorInverted";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API, "inverted=%s", inverted);
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }

        driveMotor.setPositionSensorInverted(inverted);
    }   //setPositionSensorInverted

    /**
     * This method enables/disables soft limit switches.
     *
     * @param lowerLimitEnabled specifies true to enable lower soft limit switch, false otherwise.
     * @param upperLimitEnabled specifies true to enable upper soft limit switch, false otherwise.
     */
    @Override
    public void setSoftLimitEnabled(boolean lowerLimitEnabled, boolean upperLimitEnabled)
    {
        throw new UnsupportedOperationException("Drive wheel does not support soft limits.");
    }   //setSoftLimitEnabled

    /**
     * This method sets the lower soft limit.
     *
     * @param position specifies the position of the lower limit.
     */
    @Override
    public void setSoftLowerLimit(double position)
    {
        throw new UnsupportedOperationException("Drive wheel does not support soft limits.");
    }   //setSoftLowerLimit

    /**
     * This method sets the upper soft limit.
     *
     * @param position specifies the position of the upper limit.
     */
    @Override
    public void setSoftUpperLimit(double position)
    {
        throw new UnsupportedOperationException("Drive wheel does not support soft limits.");
    }   //setSoftUpperLimit

}   //class TrcSwerveModule
