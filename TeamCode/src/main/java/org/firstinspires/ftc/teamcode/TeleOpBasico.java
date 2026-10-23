package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * TeleOp basico do robo (BIOBUZZ).
 *
 * Controles (gamepad 1):
 *   Stick esquerdo (Y)   -> frente / tras
 *   Stick direito  (X)   -> girar
 *   Gatilho esquerdo     -> segurar para modo lento
 *
 *   Bumper direito       -> segurar: coletor para dentro
 *   Gatilho direito      -> segurar: coletor para fora (cospe)
 *
 *   Y (triangulo)        -> liga / desliga o lancador (velocidade alvo)
 *   Bumper esquerdo      -> segurar: empurra a bolinha no lancador (servoLancador)
 *   X (quadrado)         -> segurar: lancador ao contrario (desentalar)
 *
 *   A (cruz)             -> alavanca da FLOWER para baixo
 *   B (circulo)          -> alavanca da FLOWER para cima
 *
 * Para trocar a tracao para o modo "tanque" (um stick por lado), use a linha
 * comentada dentro do loop.
 */
@TeleOp(name = "TeleOp Basico", group = "Time")
public class TeleOpBasico extends LinearOpMode {

    private final RobotHardware robot = new RobotHardware(this);
    private final ElapsedTime runtime = new ElapsedTime();

    private boolean launcherLigado = false;

    @Override
    public void runOpMode() {
        robot.init();

        telemetry.addData("Status", "Pronto. Aperte START.");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {

            // ---------- Tracao ----------
            // Stick para frente retorna valor negativo, por isso o sinal invertido.
            double drive = -gamepad1.left_stick_y;
            double turn  =  gamepad1.right_stick_x;

            boolean modoLento = gamepad1.left_trigger > 0.5;
            double factor = modoLento ? RobotHardware.SLOW_MODE_FACTOR : 1.0;

            // Modo arcade (padrao):
            robot.drive(drive * factor, turn * factor);

            // Modo tanque (um stick por lado). Para usar, comente a linha acima e descomente esta:
            // robot.setDrivePower(-gamepad1.left_stick_y * factor, -gamepad1.right_stick_y * factor);

            // ---------- Coletor ----------
            if (gamepad1.right_bumper) {
                robot.intakeIn();
            } else if (gamepad1.right_trigger > 0.5) {
                robot.intakeOut();
            } else {
                robot.intakeStop();
            }

            // ---------- Lancador ----------
            // Y alterna ligado/desligado (so na borda do clique, nao enquanto segura).
            if (gamepad1.yWasPressed()) {
                launcherLigado = !launcherLigado;
            }

            if (gamepad1.x) {
                robot.launcherReverse();
            } else if (launcherLigado) {
                robot.launcherOn();
            } else {
                robot.launcherStop();
            }

            // Bumper esquerdo empurra a bolinha; ao soltar, o empurrador recolhe.
            if (gamepad1.left_bumper) {
                robot.feederPush();
            } else {
                robot.feederRest();
            }

            // ---------- Alavanca da FLOWER ----------
            if (gamepad1.a) {
                robot.flowerDown();
            } else if (gamepad1.b) {
                robot.flowerUp();
            }

            // ---------- Telemetria ----------
            telemetry.addData("Tempo", runtime.toString());
            telemetry.addData("Drive / Turn", "%.2f / %.2f", drive, turn);
            telemetry.addData("Modo lento", modoLento ? "SIM" : "nao");
            telemetry.addData("Lancador", "%s | %.0f RPM (alvo %.0f) %s",
                    launcherLigado ? "LIGADO" : "desligado",
                    robot.getLauncherRpm(),
                    RobotHardware.LAUNCHER_TARGET_RPM,
                    (launcherLigado && robot.isLauncherAtSpeed()) ? "<< PRONTO" : "");
            telemetry.addData("FLOWER servo", "%.2f", robot.getFlowerPosition());
            telemetry.addData("Lancador esq / empurrador",
                    (robot.hasLauncherLeft() ? "ok" : "NAO configurado") + " / " +
                    (robot.hasFeeder() ? "ok" : "NAO configurado"));
            telemetry.addData("Encoders E/D", "%d / %d",
                    robot.getLeftPosition(), robot.getRightPosition());
            telemetry.update();
        }

        robot.stopAll();
    }
}
