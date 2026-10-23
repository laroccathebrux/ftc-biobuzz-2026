package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

/**
 * Autoteste: gira cada motor e servo em sequencia, sozinho, para conferir
 * sentido de rotacao e encoders. ROBO SUSPENSO antes de rodar!
 *
 * Usa os mesmos nomes e sentidos de RobotHardware. Para cada passo, a
 * telemetria diz o que deveria acontecer; anote o que aconteceu de fato.
 * O resultado dos encoders tambem vai para o log (tag "TesteMotores").
 */
@TeleOp(name = "Teste Motores (robo suspenso)", group = "Time")
public class TesteMotores extends LinearOpMode {

    private static final String TAG = "TesteMotores";
    private static final double POT = 0.3;      // potencia baixa para os testes
    private static final long   DUR_MS = 2500;  // duracao de cada passo

    @Override
    public void runOpMode() {
        DcMotorEx motorEsq = hardwareMap.get(DcMotorEx.class, RobotHardware.LEFT_DRIVE);
        DcMotorEx motorDir = hardwareMap.get(DcMotorEx.class, RobotHardware.RIGHT_DRIVE);
        DcMotorEx intake   = hardwareMap.get(DcMotorEx.class, RobotHardware.INTAKE);
        DcMotorEx lancDir  = hardwareMap.get(DcMotorEx.class, RobotHardware.LAUNCHER_RIGHT);
        DcMotorEx lancEsq  = hardwareMap.tryGet(DcMotorEx.class, RobotHardware.LAUNCHER_LEFT);
        Servo flower       = hardwareMap.get(Servo.class, RobotHardware.FLOWER_SERVO);
        Servo feeder       = hardwareMap.tryGet(Servo.class, RobotHardware.FEEDER_SERVO);

        // Mesmos sentidos de RobotHardware.init()
        motorEsq.setDirection(DcMotor.Direction.REVERSE);
        motorDir.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        lancDir.setDirection(DcMotor.Direction.REVERSE);
        if (lancEsq != null) lancEsq.setDirection(DcMotor.Direction.FORWARD);

        for (DcMotorEx m : new DcMotorEx[]{motorEsq, motorDir, intake, lancDir, lancEsq}) {
            if (m == null) continue;
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        telemetry.addLine("ROBO SUSPENSO? Aperte START para comecar.");
        telemetry.addData("Lancador esq", lancEsq != null ? "ok" : "NAO configurado");
        telemetry.addData("Servo lancador", feeder != null ? "ok" : "NAO configurado");
        telemetry.update();
        RobotLog.ii(TAG, "Pronto. lancEsq=%s feeder=%s", lancEsq != null, feeder != null);

        waitForStart();
        if (!opModeIsActive()) return;

        testaMotor(motorEsq, "1/7 Roda ESQUERDA", "deve girar para FRENTE");
        testaMotor(motorDir, "2/7 Roda DIREITA", "deve girar para FRENTE");
        testaMotor(intake,   "3/7 Coletor", "deve puxar a bolinha para DENTRO");
        testaMotor(lancDir,  "4/7 Lancador DIREITO", "deve girar no sentido de LANCAR");
        if (lancEsq != null) {
            testaMotor(lancEsq, "5/7 Lancador ESQUERDO", "deve girar no sentido de LANCAR");
        } else {
            mostra("5/7 Lancador ESQUERDO", "PULADO: nao esta na configuracao", 1500);
        }

        mostra("6/7 Servo FLOWER", "indo para BAIXO (" + RobotHardware.FLOWER_DOWN + ")", 500);
        flower.setPosition(RobotHardware.FLOWER_DOWN);
        espera(DUR_MS);
        mostra("6/7 Servo FLOWER", "voltando para CIMA (" + RobotHardware.FLOWER_UP + ")", 500);
        flower.setPosition(RobotHardware.FLOWER_UP);
        espera(DUR_MS);
        RobotLog.ii(TAG, "Servo FLOWER: baixo=%.2f cima=%.2f", RobotHardware.FLOWER_DOWN, RobotHardware.FLOWER_UP);

        if (feeder != null) {
            mostra("7/7 Servo LANCADOR", "EMPURRANDO (" + RobotHardware.FEEDER_PUSH + ")", 500);
            feeder.setPosition(RobotHardware.FEEDER_PUSH);
            espera(DUR_MS);
            mostra("7/7 Servo LANCADOR", "recolhendo (" + RobotHardware.FEEDER_REST + ")", 500);
            feeder.setPosition(RobotHardware.FEEDER_REST);
            espera(DUR_MS);
        } else {
            mostra("7/7 Servo LANCADOR", "PULADO: nao esta na configuracao", 1500);
        }

        RobotLog.ii(TAG, "FIM do teste");
        while (opModeIsActive()) {
            telemetry.addLine("TESTE CONCLUIDO. Aperte STOP.");
            telemetry.update();
            sleep(100);
        }
    }

    private void testaMotor(DcMotorEx m, String titulo, String esperado) {
        int antes = m.getCurrentPosition();
        mostra(titulo, esperado, 1500);
        m.setPower(POT);
        long fim = System.currentTimeMillis() + DUR_MS;
        while (opModeIsActive() && System.currentTimeMillis() < fim) {
            telemetry.addData("Passo", titulo);
            telemetry.addData("Esperado", esperado);
            telemetry.addData("Encoder", m.getCurrentPosition() - antes);
            telemetry.addData("Corrente (A)", "%.2f", m.getCurrent(org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS));
            telemetry.update();
            sleep(50);
        }
        m.setPower(0);
        int delta = m.getCurrentPosition() - antes;
        RobotLog.ii(TAG, "%s | potencia +%.1f por %d ms | encoder delta=%d | %s",
                titulo, POT, DUR_MS, delta,
                delta > 50 ? "encoder OK (conta para cima)" :
                delta < -50 ? "ATENCAO: encoder conta para BAIXO (fios do encoder invertidos ou direcao)" :
                "ATENCAO: encoder nao mudou (sem encoder ligado ou motor nao girou)");
        espera(800);
    }

    private void mostra(String titulo, String msg, long ms) {
        telemetry.addData("Passo", titulo);
        telemetry.addData("Esperado", msg);
        telemetry.update();
        RobotLog.ii(TAG, "%s: %s", titulo, msg);
        espera(ms);
    }

    private void espera(long ms) {
        long fim = System.currentTimeMillis() + ms;
        while (opModeIsActive() && System.currentTimeMillis() < fim) {
            sleep(20);
        }
    }
}
