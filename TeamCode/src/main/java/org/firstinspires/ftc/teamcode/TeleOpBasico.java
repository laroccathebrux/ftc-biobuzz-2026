package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * TeleOp principal do robo (BIOBUZZ). Esquema de controle definido pelo time:
 *
 *   Gatilho direito (RT)      -> frente (proporcional)
 *   Gatilho esquerdo (LT)     -> re (proporcional)
 *   Bumper direito (RB)       -> frente a 100%
 *   Stick esquerdo (X)        -> curva
 *   X                         -> segurar: lancador (os DOIS motores) na velocidade alvo
 *   A                         -> alterna o servo da FLOWER: baixa / levanta
 *   B                         -> gira o robo 180 graus sozinho (usa o IMU)
 *
 * Botoes livres, usados assim:
 *   Bumper esquerdo (LB)      -> segurar: servo do lancador empurra a bolinha
 *   Digital para cima         -> alterna o coletor ligado / desligado (comeca LIGADO)
 *   Y                         -> segurar: coletor ao contrario (cospe)
 *   Digital para baixo        -> cancela o giro de 180 (mexer nos gatilhos tambem cancela)
 */
@TeleOp(name = "TeleOp Time", group = "Time")
public class TeleOpBasico extends LinearOpMode {

    // ---------- Ajustes do giro de 180 ----------
    private static final double GIRO_KP          = 0.012; // potencia por grau de erro
    private static final double GIRO_POT_MAX     = 0.6;
    private static final double GIRO_POT_MIN     = 0.18;  // minimo para o robo sair do lugar
    private static final double GIRO_TOLERANCIA  = 3.0;   // graus
    private static final double GIRO_TIMEOUT_S   = 3.0;

    private final RobotHardware robot = new RobotHardware(this);
    private final ElapsedTime runtime = new ElapsedTime();
    private final ElapsedTime tempoGiro = new ElapsedTime();

    private boolean flowerBaixo   = false;
    private boolean coletorLigado = true;
    private boolean girando       = false;
    private double  alvoGiro      = 0;

    @Override
    public void runOpMode() {
        robot.init();

        telemetry.addData("Status", "Pronto. Aperte START.");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {

            // ---------- Entradas de tracao ----------
            double potencia = gamepad1.right_trigger - gamepad1.left_trigger;
            if (gamepad1.right_bumper) {
                potencia = 1.0;
            }
            double curva = gamepad1.left_stick_x;
            boolean pilotoMexeu = Math.abs(potencia) > 0.1 || Math.abs(curva) > 0.1;

            // ---------- Giro de 180 graus ----------
            if (gamepad1.bWasPressed() && !girando) {
                girando  = true;
                alvoGiro = RobotHardware.normalizaAngulo(robot.getYaw() + 180.0);
                tempoGiro.reset();
            }
            if (girando && (gamepad1.dpadDownWasPressed() || pilotoMexeu
                    || tempoGiro.seconds() > GIRO_TIMEOUT_S)) {
                girando = false;
            }

            if (girando) {
                double erro = RobotHardware.normalizaAngulo(alvoGiro - robot.getYaw());
                if (Math.abs(erro) <= GIRO_TOLERANCIA) {
                    girando = false;
                    robot.stopDrive();
                } else {
                    double p = Range.clip(erro * GIRO_KP, -GIRO_POT_MAX, GIRO_POT_MAX);
                    if (Math.abs(p) < GIRO_POT_MIN) {
                        p = Math.signum(p) * GIRO_POT_MIN;
                    }
                    // Erro positivo = precisa girar para a ESQUERDA (anti-horario):
                    // roda esquerda para tras, roda direita para frente.
                    robot.setDrivePower(-p, p);
                }
            } else {
                double esq = Range.clip(potencia + curva, -1.0, 1.0);
                double dir = Range.clip(potencia - curva, -1.0, 1.0);
                robot.setDrivePower(esq, dir);
            }

            // ---------- Coletor ----------
            if (gamepad1.dpadUpWasPressed()) {
                coletorLigado = !coletorLigado;
            }
            if (gamepad1.y) {
                robot.intakeOut();
            } else if (coletorLigado) {
                robot.intakeIn();
            } else {
                robot.intakeStop();
            }

            // ---------- Lancador (X segurando aciona os dois motores) ----------
            if (gamepad1.x) {
                robot.launcherOn();
            } else {
                robot.launcherStop();
            }

            // Servo do lancador: LB empurra, soltar recolhe.
            if (gamepad1.left_bumper) {
                robot.feederPush();
            } else {
                robot.feederRest();
            }

            // ---------- Servo da FLOWER (A alterna) ----------
            if (gamepad1.aWasPressed()) {
                flowerBaixo = !flowerBaixo;
                if (flowerBaixo) {
                    robot.flowerDown();
                } else {
                    robot.flowerUp();
                }
            }

            // ---------- Telemetria ----------
            telemetry.addData("Tempo", runtime.toString());
            telemetry.addData("Tracao", "pot %.2f  curva %.2f", potencia, curva);
            telemetry.addData("Yaw (IMU)", "%.1f graus%s", robot.getYaw(),
                    girando ? String.format("  GIRANDO para %.0f", alvoGiro) : "");
            telemetry.addData("Lancador", "%s | %.0f RPM (alvo %.0f)%s",
                    gamepad1.x ? "LIGADO" : "desligado",
                    robot.getLauncherRpm(), RobotHardware.LAUNCHER_TARGET_RPM,
                    (gamepad1.x && robot.isLauncherAtSpeed()) ? "  << PRONTO" : "");
            telemetry.addData("Coletor", gamepad1.y ? "AO CONTRARIO" : (coletorLigado ? "ligado" : "desligado"));
            telemetry.addData("FLOWER", flowerBaixo ? "BAIXO" : "cima");
            telemetry.addData("Faltando na config",
                    (robot.hasLauncherLeft() ? "" : "motorLancadorEsq ") +
                    (robot.hasFeeder() ? "" : "servoLancador"));
            telemetry.update();
        }

        robot.stopAll();
    }
}
