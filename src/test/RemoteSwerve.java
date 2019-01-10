package test;

import trclib.*;

public class RemoteSwerve
{
    private final TrcGyro gyro;
    private final TrcSwerveModule lfModule;
    private final TrcSwerveModule rfModule;
    private final TrcSwerveModule lrModule;
    private final TrcSwerveModule rrModule;
    private final TrcSwerveDriveBase driveBase;
    private final TrcTaskMgr taskMgr;

    public RemoteSwerve(double width, double length)
    {
        new TrcTaskMgr();

        TrcPidController.PidCoefficients pidCoefficients = new TrcPidController.PidCoefficients(0.02);
        double turnTolerance = 1;

        gyro = new MockGyro("Gyro");

        MockMotorController lfMotor = new MockMotorController(53);
        MockMotorController rfMotor = new MockMotorController(53);
        MockMotorController lrMotor = new MockMotorController(53);
        MockMotorController rrMotor = new MockMotorController(53);

        TrcPidController lfCtrl = new TrcPidController("LFPID", pidCoefficients, turnTolerance, lfMotor::getPosition);
        TrcPidController rfCtrl = new TrcPidController("RFPID", pidCoefficients, turnTolerance, rfMotor::getPosition);
        TrcPidController lrCtrl = new TrcPidController("LRPID", pidCoefficients, turnTolerance, lrMotor::getPosition);
        TrcPidController rrCtrl = new TrcPidController("RRPID", pidCoefficients, turnTolerance, rrMotor::getPosition);

        TrcDigitalInput in = new TrcDigitalInput("Input")
        {
            @Override
            public boolean isActive()
            {
                return false;
            }
        };


        TrcPidActuator lfActuator = new TrcPidActuator("LF_ACT", lfMotor, in, lfCtrl, 0.1);
        TrcPidActuator rfActuator = new TrcPidActuator("RF_ACT", rfMotor, in, rfCtrl, 0.1);
        TrcPidActuator lrActuator = new TrcPidActuator("LR_ACT", lrMotor, in, lrCtrl, 0.1);
        TrcPidActuator rrActuator = new TrcPidActuator("RR_ACT", rrMotor, in, rrCtrl, 0.1);

        lfModule = new TrcSwerveModule("lfModule", new MockMotorController(3600), lfActuator);
        rfModule = new TrcSwerveModule("lfModule", new MockMotorController(3600), rfActuator);
        lrModule = new TrcSwerveModule("lfModule", new MockMotorController(3600), lrActuator);
        rrModule = new TrcSwerveModule("lfModule", new MockMotorController(3600), rrActuator);

        driveBase = new TrcSwerveDriveBase(lfModule, lrModule, rfModule, rrModule, gyro, width, length);

        taskMgr = TrcTaskMgr.getInstance();
    }

    public SwerveStatus getStatus(double x, double y, double turn, double gyroAngle)
    {
        taskMgr.executeTaskType(TrcTaskMgr.TaskType.PRECONTINUOUS_TASK, TrcRobot.RunMode.TELEOP_MODE);

        driveBase.holonomicDrive(x, y, turn, gyroAngle);

        SwerveStatus status = new SwerveStatus();
        status.lfPower = (float) lfModule.getPower();
        status.rfPower = (float) rfModule.getPower();
        status.lrPower = (float) lrModule.getPower();
        status.rrPower = (float) rrModule.getPower();

        status.lfAngle = (float) lfModule.getSteerAngle();
        status.rfAngle = (float) rfModule.getSteerAngle();
        status.lrAngle = (float) lrModule.getSteerAngle();
        status.rrAngle = (float) rrModule.getSteerAngle();

        taskMgr.executeTaskType(TrcTaskMgr.TaskType.POSTCONTINUOUS_TASK, TrcRobot.RunMode.TELEOP_MODE);

        return status;
    }

    private static class SwerveStatus
    {
        public float lfPower, lfAngle;
        public float rfPower, rfAngle;
        public float lrPower, lrAngle;
        public float rrPower, rrAngle;
    }
}