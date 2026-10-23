package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Classe que concentra todo o hardware do robo (temporada BIOBUZZ 2026-27).
 *
 * Todos os OpModes (TeleOp e Autonomo) usam esta classe, assim a configuracao
 * dos motores, servos e sensores fica em um unico lugar.
 *
 * IMPORTANTE: os nomes passados ao hardwareMap (ex.: "left_drive") precisam
 * ser exatamente iguais aos nomes definidos na configuracao do robo no Driver Station.
 *
 * Robo:
 *  - Tracao tanque: 1 motor HD Hex (UltraPlanetary) de cada lado
 *  - Coletor (intake): 1 motor
 *  - Lancador (flywheel): 2 motores
 *  - Alavanca da FLOWER: 1 servo (empurra o POLLEN para fora da FLOWER)
 *  - Webcam Logitech C270: ver RobotVision
 */
public class RobotHardware {

    // =====================================================================
    //  Nomes na configuracao do Driver Station (arquivo "autonomo.xml")
    //
    //  As portas NAO sao definidas no codigo: elas ficam na configuracao do
    //  robo (Driver Station > Configure Robot). O que o codigo usa e o NOME.
    //  Os nomes abaixo sao os que o time ja cadastrou na configuracao.
    //
    //  Nome              Hub             Porta       Tipo na configuracao
    //  ----------------  --------------  ----------  ---------------------------------
    //  motorEsq          Control Hub     Motor 0     REV 40:1 HD Hex Motor
    //  motorExtra        Control Hub     Motor 1     REV Core Hex Motor (coletor)
    //  motorDir          Control Hub     Motor 2     REV 40:1 HD Hex Motor
    //  motorLancador     Control Hub     Motor 3     REV 40:1 HD Hex Motor (lancador direito)
    //  motorLancadorEsq  Expansion Hub   Motor 0     (AINDA NAO CADASTRADO: lancador esquerdo)
    //  servo             Control Hub     Servo 0     Servo (alavanca da FLOWER)
    //  servoLancador     Control Hub     Servo 1     Servo (empurra a bolinha no lancador)
    //  imu               Control Hub     interno     IMU BHI260AP (giroscopio do hub)
    //  Webcam 1          Control Hub     USB         (AINDA NAO CADASTRADA: Logitech C270)
    // =====================================================================
    public static final String LEFT_DRIVE     = "motorEsq";          // Control Hub, motor 0
    public static final String RIGHT_DRIVE    = "motorDir";          // Control Hub, motor 2
    public static final String INTAKE         = "motorExtra";        // Control Hub, motor 1
    public static final String LAUNCHER_LEFT  = "motorLancadorEsq";  // Expansion Hub, motor 0
    public static final String LAUNCHER_RIGHT = "motorLancador";     // Control Hub, motor 3
    public static final String FLOWER_SERVO   = "servo";             // Control Hub, servo 0
    public static final String FEEDER_SERVO   = "servoLancador";     // Control Hub, servo 1

    // =====================================================================
    //  Constantes de ajuste (tunar no robo real)
    // =====================================================================

    /** Fator aplicado a tracao no modo "lento". */
    public static final double SLOW_MODE_FACTOR = 0.4;

    /** Potencia do coletor (0 a 1). */
    public static final double INTAKE_POWER = 1.0;

    /**
     * Encoder do HD Hex: 28 ticks por volta do eixo do motor.
     * Se o lancador for acoplado direto no motor (sem caixa de reducao),
     * uma volta da roda do lancador = 28 ticks.
     */
    public static final double LAUNCHER_TICKS_PER_REV = 28.0;

    /**
     * Velocidade alvo do lancador em RPM do eixo do motor (maximo ~6000).
     * O codigo antigo do time usava potencia 0.8, que equivale a ~4800 RPM sem carga.
     * AJUSTAR testando a distancia de tiro.
     */
    public static final double LAUNCHER_TARGET_RPM = 4500.0;

    /** Tolerancia (em RPM) para considerar o lancador "na velocidade". */
    public static final double LAUNCHER_RPM_TOLERANCE = 100.0;

    /** Posicoes do servo da FLOWER (0 a 1). Valores vindos do codigo antigo do time. */
    public static final double FLOWER_UP   = 1.0;   // posicao inicial
    public static final double FLOWER_DOWN = 0.63;  // posicao "girada"

    /** Posicoes do servo que empurra a bolinha no lancador. Valores do codigo antigo. */
    public static final double FEEDER_REST = 0.0;
    public static final double FEEDER_PUSH = 1.0;

    /**
     * Tracao: HD Hex (28 ticks/volta do eixo do motor) com caixa de reducao.
     * A configuracao do robo cadastra os motores como "40:1 HD Hex", entao
     * assumimos 40:1 = 1120 ticks por volta da roda. CONFIRMAR olhando a
     * etiqueta da caixa de reducao (40:1 fixa ou UltraPlanetary com cartuchos).
     * Roda do kit REV: 90 mm de diametro.
     */
    public static final double DRIVE_TICKS_PER_MOTOR_REV = 28.0;
    public static final double DRIVE_GEAR_RATIO          = 40.0;
    public static final double WHEEL_DIAMETER_CM         = 9.0;
    public static final double DRIVE_TICKS_PER_CM =
            (DRIVE_TICKS_PER_MOTOR_REV * DRIVE_GEAR_RATIO) / (Math.PI * WHEEL_DIAMETER_CM);

    // =====================================================================
    //  Dispositivos
    // =====================================================================
    private DcMotorEx leftDrive;
    private DcMotorEx rightDrive;
    private DcMotorEx intake;
    private DcMotorEx launcherLeft;
    private DcMotorEx launcherRight;
    private Servo flowerServo;
    private Servo feederServo;   // opcional: pode nao existir no robo

    private final OpMode opMode;

    public RobotHardware(OpMode opMode) {
        this.opMode = opMode;
    }

    /**
     * Inicializa todo o hardware. Chamar uma unica vez no inicio do OpMode.
     */
    public void init() {
        HardwareMap hw = opMode.hardwareMap;

        // ---------- Tracao ----------
        leftDrive  = hw.get(DcMotorEx.class, LEFT_DRIVE);
        rightDrive = hw.get(DcMotorEx.class, RIGHT_DRIVE);

        // Os motores ficam espelhados no chassi, entao um lado precisa ser invertido.
        // Se o robo andar para tras ao empurrar o stick para frente, troque os dois.
        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        setDriveZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // ---------- Coletor ----------
        intake = hw.get(DcMotorEx.class, INTAKE);
        intake.setDirection(DcMotor.Direction.FORWARD); // inverter se coletar ao contrario
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // ---------- Lancador ----------
        launcherRight = hw.get(DcMotorEx.class, LAUNCHER_RIGHT);
        // O motor esquerdo fica no Expansion Hub. tryGet() devolve null se ele ainda
        // nao estiver na configuracao, e o robo continua funcionando com um motor so.
        launcherLeft  = hw.tryGet(DcMotorEx.class, LAUNCHER_LEFT);

        // Os dois motores ficam um de cada lado da bolinha, entao giram em sentidos opostos.
        // Se a bolinha for para tras, inverta os dois.
        launcherRight.setDirection(DcMotor.Direction.REVERSE);
        // FLOAT: o lancador desacelera sozinho em vez de frear bruscamente.
        launcherRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        // Controle de velocidade por encoder (setVelocity exige RUN_USING_ENCODER).
        launcherRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launcherRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        if (launcherLeft != null) {
            launcherLeft.setDirection(DcMotor.Direction.FORWARD);
            launcherLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            launcherLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            launcherLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        } else {
            opMode.telemetry.addData("AVISO", "'" + LAUNCHER_LEFT + "' nao esta na configuracao");
        }

        // ---------- Servo da FLOWER ----------
        flowerServo = hw.get(Servo.class, FLOWER_SERVO);
        flowerServo.setPosition(FLOWER_UP);

        // ---------- Servo do lancador (opcional) ----------
        feederServo = hw.tryGet(Servo.class, FEEDER_SERVO);
        if (feederServo != null) {
            feederServo.setPosition(FEEDER_REST);
        }

        opMode.telemetry.addData("Hardware", "Inicializado");
    }

    // =====================================================================
    //  Tracao
    // =====================================================================

    /**
     * Move o robo combinando avanco e rotacao (modo "arcade").
     *
     * @param drive frente/tras (-1 a 1, positivo = frente)
     * @param turn  rotacao     (-1 a 1, positivo = horario)
     */
    public void drive(double drive, double turn) {
        double left  = drive + turn;
        double right = drive - turn;

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

    public int getLeftPosition()  { return leftDrive.getCurrentPosition(); }
    public int getRightPosition() { return rightDrive.getCurrentPosition(); }

    /** Converte centimetros em ticks de encoder da tracao (util no autonomo). */
    public static int cmToTicks(double cm) {
        return (int) Math.round(cm * DRIVE_TICKS_PER_CM);
    }

    // =====================================================================
    //  Coletor
    // =====================================================================

    /** Liga o coletor para dentro. */
    public void intakeIn() {
        intake.setPower(INTAKE_POWER);
    }

    /** Inverte o coletor (cospe a bolinha). */
    public void intakeOut() {
        intake.setPower(-INTAKE_POWER);
    }

    public void intakeStop() {
        intake.setPower(0);
    }

    // =====================================================================
    //  Lancador
    // =====================================================================

    /** Gira o lancador na velocidade alvo (LAUNCHER_TARGET_RPM). */
    public void launcherOn() {
        setLauncherRpm(LAUNCHER_TARGET_RPM);
    }

    /** Gira o lancador em uma velocidade especifica, em RPM. */
    public void setLauncherRpm(double rpm) {
        double ticksPerSec = rpm * LAUNCHER_TICKS_PER_REV / 60.0;
        launcherRight.setVelocity(ticksPerSec);
        if (launcherLeft != null) launcherLeft.setVelocity(ticksPerSec);
    }

    /** Gira o lancador ao contrario, devagar (para desentalar bolinha). */
    public void launcherReverse() {
        launcherRight.setPower(-0.5);
        if (launcherLeft != null) launcherLeft.setPower(-0.5);
    }

    public void launcherStop() {
        launcherRight.setVelocity(0);
        if (launcherLeft != null) launcherLeft.setVelocity(0);
    }

    /** Velocidade media atual do lancador, em RPM (do eixo do motor). */
    public double getLauncherRpm() {
        double ticksPerSec = launcherRight.getVelocity();
        if (launcherLeft != null) {
            ticksPerSec = (ticksPerSec + launcherLeft.getVelocity()) / 2.0;
        }
        return ticksPerSec * 60.0 / LAUNCHER_TICKS_PER_REV;
    }

    /** True se o motor esquerdo do lancador foi encontrado na configuracao. */
    public boolean hasLauncherLeft() {
        return launcherLeft != null;
    }

    /** True quando o lancador esta dentro da tolerancia da velocidade alvo. */
    public boolean isLauncherAtSpeed() {
        return Math.abs(getLauncherRpm() - LAUNCHER_TARGET_RPM) <= LAUNCHER_RPM_TOLERANCE;
    }

    // =====================================================================
    //  Servo da FLOWER
    // =====================================================================

    public void flowerUp() {
        flowerServo.setPosition(FLOWER_UP);
    }

    public void flowerDown() {
        flowerServo.setPosition(FLOWER_DOWN);
    }

    public double getFlowerPosition() {
        return flowerServo.getPosition();
    }

    // =====================================================================
    //  Servo do lancador (empurra a bolinha para a roda)
    // =====================================================================

    public boolean hasFeeder() {
        return feederServo != null;
    }

    /** Empurra a bolinha para dentro do lancador. */
    public void feederPush() {
        if (feederServo != null) feederServo.setPosition(FEEDER_PUSH);
    }

    /** Recolhe o empurrador para receber a proxima bolinha. */
    public void feederRest() {
        if (feederServo != null) feederServo.setPosition(FEEDER_REST);
    }

    /** Para tudo que se move. Chamar ao final do OpMode. */
    public void stopAll() {
        stopDrive();
        intakeStop();
        launcherStop();
    }
}
