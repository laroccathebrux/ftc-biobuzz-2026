package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Classe que concentra todo o hardware do robo.
 *
 * Todos os OpModes (TeleOp e Autonomo) usam esta classe, assim a configuracao
 * dos motores, servos e sensores fica em um unico lugar.
 *
 * IMPORTANTE: os nomes passados ao hardwareMap (ex.: "left_drive") precisam
 * ser exatamente iguais aos nomes definidos na configuracao do robo no Driver Station.
 *
 * Drivetrain: tanque (diferencial) com um motor de cada lado.
 * Se o robo tiver dois motores por lado, adicionar os motores extras aqui.
 */
public class RobotHardware {

    // ---------- Nomes na configuracao do Driver Station ----------
    public static final String LEFT_DRIVE  = "left_drive";
    public static final String RIGHT_DRIVE = "right_drive";

    // ---------- Constantes de ajuste ----------
    /** Fator aplicado a potencia dos motores no modo "lento" do TeleOp. */
    public static final double SLOW_MODE_FACTOR = 0.4;

    // ---------- Dispositivos ----------
    private DcMotorEx leftDrive;
    private DcMotorEx rightDrive;

    // TODO: adicionar aqui servos, motores de mecanismos e sensores
    //       quando os detalhes do robo forem definidos.

    private final OpMode opMode;

    public RobotHardware(OpMode opMode) {
        this.opMode = opMode;
    }

    /**
     * Inicializa todo o hardware. Chamar uma unica vez no inicio do OpMode.
     */
    public void init() {
        HardwareMap hw = opMode.hardwareMap;

        leftDrive  = hw.get(DcMotorEx.class, LEFT_DRIVE);
        rightDrive = hw.get(DcMotorEx.class, RIGHT_DRIVE);

        // Os motores ficam espelhados no chassi, entao um lado precisa ser invertido.
        // Se o robo andar para tras ao empurrar o stick para frente, troque os dois.
        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        // Freia o motor ao receber potencia zero (em vez de deixar rodar livre).
        setDriveZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Usa encoders para controle de velocidade mais consistente.
        setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);

        opMode.telemetry.addData("Hardware", "Inicializado");
    }

    /**
     * Move o robo combinando avanco e rotacao (modo "arcade").
     *
     * @param drive frente/tras (-1 a 1, positivo = frente)
     * @param turn  rotacao     (-1 a 1, positivo = horario)
     */
    public void drive(double drive, double turn) {
        double left  = drive + turn;
        double right = drive - turn;

        // Normaliza para nenhum lado passar de 100%.
        double max = Math.max(Math.abs(left), Math.abs(right));
        if (max > 1.0) {
            left  /= max;
            right /= max;
        }

        setDrivePower(left, right);
    }

    /** Envia potencia diretamente a cada lado (modo "tanque", um stick por lado). */
    public void setDrivePower(double left, double right) {
        leftDrive.setPower(left);
        rightDrive.setPower(right);
    }

    /** Para os motores de tracao. */
    public void stopDrive() {
        setDrivePower(0, 0);
    }

    public void setDriveMode(DcMotor.RunMode mode) {
        leftDrive.setMode(mode);
        rightDrive.setMode(mode);
    }

    public void setDriveZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        leftDrive.setZeroPowerBehavior(behavior);
        rightDrive.setZeroPowerBehavior(behavior);
    }

    // ---------- Acesso aos encoders (util para autonomo e telemetria) ----------
    public int getLeftPosition()  { return leftDrive.getCurrentPosition(); }
    public int getRightPosition() { return rightDrive.getCurrentPosition(); }
}
