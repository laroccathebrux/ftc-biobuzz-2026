package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

/**
 * OpMode para testar a webcam e a deteccao de AprilTag.
 *
 * Mostra na telemetria cada tag vista, com distancia (range), angulo (bearing)
 * e posicao. Para ver a imagem da camera no Driver Station: menu (3 pontos) > Camera Stream.
 *
 * Nao move o robo.
 */
@TeleOp(name = "Teste Camera (AprilTag)", group = "Time")
public class TesteCamera extends LinearOpMode {

    private final RobotVision vision = new RobotVision(this);

    @Override
    public void runOpMode() {
        vision.init();

        telemetry.addData("Status", "Camera iniciando... Aperte START.");
        telemetry.addData("Dica", "3 pontos > Camera Stream para ver a imagem");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            telemetry.addData("Camera", vision.getCameraState());

            for (AprilTagDetection det : vision.getDetections()) {
                if (det instanceof AprilTagSingleDetection) {
                    AprilTagSingleDetection single = (AprilTagSingleDetection) det;
                    String nome = single.metadata != null ? single.metadata.name : "desconhecida";
                    telemetry.addLine(String.format("\n== Tag ID %d (%s)", single.id, nome));
                } else {
                    AprilTagClusterDetection cluster = (AprilTagClusterDetection) det;
                    telemetry.addLine(String.format("\n== Cluster %s (%d%% visivel)",
                            cluster.metadata.name, cluster.percentClusterFound));
                }

                if (det.ftcPose != null) {
                    telemetry.addLine(String.format("   dist %.1f pol | angulo %.1f graus",
                            det.ftcPose.range, det.ftcPose.bearing));
                    telemetry.addLine(String.format("   x %.1f  y %.1f  z %.1f (pol)",
                            det.ftcPose.x, det.ftcPose.y, det.ftcPose.z));
                }
            }

            telemetry.update();
            sleep(20);
        }

        vision.close();
    }
}
