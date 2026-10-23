package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * TeleOp basico para o robo tanque.
 *
 * Controles (gamepad 1):
 *   Stick esquerdo (Y)  -> frente / tras
 *   Stick direito  (X)  -> girar
 *   Bumper direito      -> segurar para modo lento
 *
 * Para trocar para o modo "tanque" (um stick por lado), use a linha
 * comentada dentro do loop.
 */
@TeleOp(name = "TeleOp Basico", group = "Time")
public class TeleOpBasico extends LinearOpMode {

    private final RobotHardware robot = new RobotHardware(this);
    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        robot.init();

        telemetry.addData("Status", "Pronto. Aperte START.");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            // Stick para frente retorna valor negativo, por isso o sinal invertido.
            double drive = -gamepad1.left_stick_y;
            double turn  =  gamepad1.right_stick_x;

            double factor = gamepad1.right_bumper ? RobotHardware.SLOW_MODE_FACTOR : 1.0;

            // Modo arcade (padrao):
            robot.drive(drive * factor, turn * factor);

            // Modo tanque (um stick por lado). Para usar, comente a linha acima e descomente esta:
            // robot.setDrivePower(-gamepad1.left_stick_y * factor, -gamepad1.right_stick_y * factor);

            telemetry.addData("Tempo", runtime.toString());
            telemetry.addData("Drive / Turn", "%.2f / %.2f", drive, turn);
            telemetry.addData("Modo lento", gamepad1.right_bumper ? "SIM" : "nao");
            telemetry.addData("Encoders E/D", "%d / %d",
                    robot.getLeftPosition(), robot.getRightPosition());
            telemetry.update();
        }

        robot.stopDrive();
    }
}
