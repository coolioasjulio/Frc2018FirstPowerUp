package team492;

import team492.PixyVision.TargetInfo;
import trclib.TrcRobot;
import trclib.TrcStateMachine;

public class CmdStrafeUntilCube implements TrcRobot.RobotCommand
{
    private static final String moduleName = "CmdStrafeUntilCube";

    public enum State
    {
        START_STRAFE, CHECK_FOR_CUBE, DONE
    }

    private Robot robot;
    private TrcStateMachine<State> sm;
    private StopTrigger stopTrigger;
    private boolean strafeRight;
    private double startTime;
    private double startX, startY;

    public CmdStrafeUntilCube(Robot robot)
    {
        this.robot = robot;
        sm = new TrcStateMachine<>(moduleName);
    }

    /**
     * Start the task strafing in the specified direction. Signal event when
     * done.
     * 
     * @param strafeRight If true, strafe right. If false, strafe left.
     */
    public void start(boolean strafeRight)
    {
        start(strafeRight, this::shouldStop);
    }

    /**
     * @param strafeRight
     * @param stopTrigger Should return true when this should stop
     */
    public void start(boolean strafeRight, StopTrigger stopTrigger)
    {
        this.strafeRight = strafeRight;
        sm.start(State.START_STRAFE);

        startTime = Robot.getModeElapsedTime();
        startX = robot.driveBase.getXPosition();
        startY = robot.driveBase.getYPosition();
    }

    /**
     * Default stopTrigger. Continue indefinitely.
     */
    private boolean shouldStop(double elapsedTime, double changeX, double changeY)
    {
        return false;
    }

    /**
     * Cancel the task. Use {@code distanceMoved()} to see how much the robot has moved
     */
    public void stop()
    {
        robot.pidDrive.cancel();
        sm.stop();
    }

    /**
     * Is this task current active?
     * 
     * @return True if active, false if not.
     */
    public boolean isRunning()
    {
        return sm.isEnabled();
    }

    /**
     * How much time has passed since this task was started?
     * 
     * @return Time in seconds
     */
    public double elapsedTime()
    {
        return Robot.getModeElapsedTime() - startTime;
    }

    /**
     * Return distance moved since this task was started
     * 
     * @return scaled distance
     */
    public double[] distanceMoved()
    {
        return new double[] { changeX(), changeY() };
    }

    public double changeX()
    {
        robot.tracer.traceInfo("changeX", "changeX");
        return robot.driveBase.getXPosition() - startX;
    }

    private double changeY()
    {
        robot.tracer.traceInfo("changeY", "changeY");
        return robot.driveBase.getYPosition() - startY;
    }

    @Override
    public boolean cmdPeriodic(double elapsedTime)
    {
        boolean done = !sm.isEnabled();

        if (done) return true;

        State state = sm.checkReadyAndGetState();

        //
        // Print debug info.
        //
        robot.dashboard.displayPrintf(1, "State: %s", state == null? "NotReady": state);

        if(stopTrigger==null)
        {
            robot.tracer.traceInfo("nullTrigger", "nullTrigger");
        }
        if (stopTrigger.shouldStop(elapsedTime(), changeX(), changeY()))
        {
            stop();
            done = true;
        }
        else if (state != null)
        {
            switch (state)
            {
                case START_STRAFE:
                    double xPower = RobotInfo.FIND_CUBE_STRAFE_POWER * (strafeRight ? 1 : -1);
                    robot.tracer.traceInfo("XPower", "xPower=%.2f", xPower);
                    robot.pidDrive.driveMaintainHeading(xPower, 0.0, robot.targetHeading);
                    robot.tracer.traceInfo("strafing", "driveMaintaintHeading");
                    sm.setState(State.CHECK_FOR_CUBE);
                    break;
                case CHECK_FOR_CUBE:
                    TargetInfo target = robot.pixy.getTargetInfo();
                    if (target != null && Math.abs(target.angle) <= RobotInfo.FIND_CUBE_ANGLE_TOLERANCE)
                    {
                        sm.setState(State.DONE);
                    }
                    break;
                case DONE:
                    robot.pidDrive.cancel();
                    done = true;
                    break;
            }
            robot.traceStateInfo(Robot.getModeElapsedTime(), state.toString());
        }
        return done;
    }

    public interface StopTrigger
    {
        public boolean shouldStop(double elapsedTime, double changeX, double changeY);
    }

}
