# Código do time — robô BIOBUZZ 2026-27

Documento de passagem para quem for continuar a programação. Tudo que está
aqui foi feito e testado no robô em 23/10/2026.

## Onde está o código

- Branch Git: `codigo-time` (o `master` é o SDK oficial da FIRST v12.0, intocado).
- Pasta do time: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`
- Nunca editar o módulo `FtcRobotController`: ele é o SDK e será substituído nas atualizações.

| Arquivo | O que é |
|---|---|
| `RobotHardware.java` | Todo o hardware em um lugar: nomes dos dispositivos, sentidos dos motores, constantes de ajuste, IMU, métodos de tração, coletor, lançador e servos. |
| `TeleOpBasico.java` | OpMode **"TeleOp Time"**, o TeleOp principal. Esquema de controle fixado pelos pilotos (abaixo). |
| `RobotVision.java` | Webcam Logitech C270 + processador de AprilTag (API v12, com clusters). |
| `TesteCamera.java` | OpMode "Teste Camera (AprilTag)": mostra as tags vistas, distância e ângulo. |
| `TesteMotores.java` | OpMode "Teste Motores (robo suspenso)": gira cada motor e servo em sequência e grava no log o delta do encoder. |
| `../legado/` | Cópia do TeleOp antigo feito em OnBot Java e da configuração do robô, só para referência. |

## O robô

- Tração tanque, 1 motor HD Hex por lado. **Sem cabo de encoder** nas rodas e no coletor (encoders marcam zero). Por isso a tração roda em `RUN_WITHOUT_ENCODER`. Se ligarem os cabos, trocar para `RUN_USING_ENCODER` em `RobotHardware.init()`.
- Coletor: 1 motor Core Hex (`motorExtra`).
- Lançador: 2 motores HD Hex (`motorLancador` no Control Hub, `motorLancadorEsq` no Expansion Hub). Têm encoder; rodam com controle de velocidade em RPM.
- Servo `servo`: alavanca da FLOWER (posições 1.0 em cima, 0.63 embaixo).
- Servo `servoLancador`: empurra a bolinha para a roda do lançador (0.0 recolhido, 1.0 empurrando).
- IMU interno do Control Hub. Hub deitado, logo para cima, portas USB para a **esquerda** do robô.
- Webcam C270 no USB-A do Control Hub.

## Configuração no Driver Station

Configuração ativa: **"rodolfo"** (a "autonomo" é uma cópia idêntica). Cópia em `TeamCode/legado/configuracao-autonomo.xml`.

| Nome | Hub | Porta | Tipo |
|---|---|---|---|
| motorEsq | Control Hub | Motor 0 | REV 40:1 HD Hex |
| motorExtra | Control Hub | Motor 1 | REV Core Hex |
| motorDir | Control Hub | Motor 2 | REV 40:1 HD Hex |
| motorLancador | Control Hub | Motor 3 | REV 40:1 HD Hex |
| servo | Control Hub | Servo 0 | Servo |
| servoLancador | Control Hub | Servo 1 | Servo |
| imu | Control Hub | interno | IMU BHI260AP |
| motorLancadorEsq | Expansion Hub 2 | Motor 0 | REV 40:1 HD Hex |
| Webcam 1 | Control Hub | USB | Webcam |

O Expansion Hub está no endereço 2, ligado ao Control Hub por XT30 (energia) e cabo RS485 de 3 pinos (dados).

## Controles do "TeleOp Time" (fixados pelos pilotos, não mudar sem combinar)

| Controle | Função |
|---|---|
| RT | frente, proporcional |
| LT | ré, proporcional |
| RB | frente a 100% |
| Stick esquerdo (X) | curva |
| X (segurar) | lançador, os dois motores, na velocidade alvo |
| A | alterna o servo da FLOWER: baixa / levanta |
| B | gira o robô 180 graus sozinho (IMU) |
| LB (segurar) | servo do lançador empurra a bolinha |
| Digital para cima | liga / desliga o coletor (começa ligado) |
| Y (segurar) | coletor ao contrário |
| Digital para baixo | cancela o giro de 180 |

A telemetria mostra a linha `Versao` (hoje `v4`), para saber qual versão está no hub.

## Constantes para ajustar (topo de `RobotHardware.java`)

- `LAUNCHER_TARGET_RPM` (4500): velocidade do lançador. Ajustar pela distância de tiro.
- `FLOWER_UP` / `FLOWER_DOWN`, `FEEDER_REST` / `FEEDER_PUSH`: posições dos servos.
- `DRIVE_GEAR_RATIO` (40) e `WHEEL_DIAMETER_CM` (9): só importam para autônomo por encoder, que hoje não é possível sem os cabos.
- Em `TeleOpBasico.java`: `GIRO_KP`, `GIRO_POT_MAX/MIN`, `GIRO_TOLERANCIA`, `GIRO_SENTIDO` (trocar o sinal se o robô "dançar" em vez de girar).

## Como compilar e instalar

Requisitos: Android Studio Narwhal 3 ou mais novo (traz o JDK). Abrir a pasta do projeto no Android Studio, módulo `TeamCode`, botão Run.

Pelo terminal, com o hub no cabo USB-C:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :TeamCode:installDebug
```

Depois de instalar, o app do Robot Controller precisa ser iniciado:

```bash
adb shell am start -n com.qualcomm.ftcrobotcontroller/org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity
```

Pelo Wi-Fi, com o robô na arena: `./deploy-wifi.sh` (compila, entra na rede do hub, instala, volta). Rede do hub: `FTC-kWD4`, senha `password`, ADB em `192.168.43.1:5555`.

Atenção: o app oficial assinado pela REV foi desinstalado do hub em 23/10/2026 para aceitar o nosso APK. Se o hub for atualizado pelo REV Hardware Client, será preciso desinstalar de novo antes de instalar o nosso (`adb uninstall com.qualcomm.ftcrobotcontroller`). A configuração do robô não se perde.

## Pendências

- Autônomo dos 30 s iniciais: lançar as bolinhas pré-carregadas no HIVE e sair da zona. Sem encoder nas rodas, usar tempo e IMU, ou ligar os cabos de encoder.
- Confirmar caixa de redução da tração (configurada como 40:1) e do lançador.
- O OnBot Java do hub ainda tem o `TeleOpChassi` antigo registrado; pode ser apagado pelo OnBot Java no painel web do hub.
- Mira por AprilTag: os clusters do HIVE se chamam `RED SCORING`, `BLUE SCORING`, `RED AUDIENCE`, `BLUE AUDIENCE`, com a origem no centro da abertura da célula. As tags se movem, então servem para mirar, não para localização no campo.
