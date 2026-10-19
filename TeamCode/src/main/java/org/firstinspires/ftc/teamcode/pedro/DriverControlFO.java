package org.firstinspires.ftc.teamcode.pedro;

import static com.pedropathing.api.Paths.line;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.hardware.motors.GoBILDA5201Series;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Light;
import com.qualcomm.robotcore.hardware.LightBlinker;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcontroller.external.samples.ConceptRevLED;
import org.firstinspires.ftc.teamcode.pedro.Subsystems.DriveTrain;

@TeleOp(name = "Driver Control Field Oriented")
public class DriverControlFO extends OpMode {
    DriveTrain driveTrain;
    private enum DriveState{
        MANUAL,
        AUTO
    }
    private DriveState currentState = DriveState.MANUAL;
    private Follower follower;
    private final PoseFactory poseFactory = PoseFactory.degrees();
    private Pose currentPose = poseFactory.of(0.0,0.0,0.0);
    private Pose shootingPose = poseFactory.of(0.0,0.0,0.0);
    private Servo RGB;


    @Override
    public void init() {
        driveTrain = new DriveTrain(hardwareMap);
        follower = Constants.create(hardwareMap);
        follower.setPose(currentPose);
        follower.update();
        RGB = hardwareMap.get(Servo.class, "indLight");

    }

    @Override
    public void start() {

    }

    @Override
    public void loop() {
        follower.update();
        switch (currentState) {
            case MANUAL:
                driveTrain.drive(gamepad1);
                if (gamepad1.xWasPressed()){
                    shootingPose = follower.pose();
                }
                else if (gamepad1.yWasPressed()){
                    currentPose = follower.pose();
                    Path pathToShooter = line(currentPose,shootingPose).linear(currentPose,shootingPose);
                    follower.follow(pathToShooter);
                    currentState = DriveState.AUTO;
                    RGB.setPosition(1.0);
                }

                break;
            case AUTO:
                gamepad1.rumble(25);
                boolean driverInterrupt = Math.abs(gamepad1.left_stick_y) > 0.1 ||
                        Math.abs(gamepad1.left_stick_x) > 0.1 ||
                        Math.abs(gamepad1.right_stick_x) > 0.1 ||
                        Math.abs(gamepad1.right_stick_y) > 0.1;
                if (!follower.isBusy() || driverInterrupt){
                    follower.stop();
                    currentState = DriveState.MANUAL;
                    RGB.setPosition(0.0);
                }
                break;
        }

        telemetry.addData("Drive State:", currentState);
        telemetry.update();

    }
}
