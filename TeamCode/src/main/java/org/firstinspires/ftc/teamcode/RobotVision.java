package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/**
 * Encapsula a webcam (Logitech) e o processador de AprilTag.
 *
 * A camera precisa estar na configuracao do Driver Station com o nome WEBCAM_NAME.
 * O SDK ja traz calibracao para os modelos Logitech mais comuns (C270, C920, etc.),
 * entao a estimativa de distancia e angulo funciona sem configuracao extra.
 *
 * Uso tipico:
 *   RobotVision vision = new RobotVision(this);
 *   vision.init();
 *   ...
 *   for (AprilTagDetection d : vision.getDetections()) { ... }
 *   ...
 *   vision.close();
 */
public class RobotVision {

    public static final String WEBCAM_NAME = "Webcam 1";

    private final OpMode opMode;
    private AprilTagProcessor aprilTag;
    private VisionPortal portal;

    public RobotVision(OpMode opMode) {
        this.opMode = opMode;
    }

    /** Abre a camera e inicia o processamento de AprilTag. */
    public void init() {
        aprilTag = new AprilTagProcessor.Builder()
                .setDrawAxes(true)
                .setDrawTagOutline(true)
                .build();

        portal = new VisionPortal.Builder()
                .setCamera(opMode.hardwareMap.get(WebcamName.class, WEBCAM_NAME))
                .addProcessor(aprilTag)
                .build();
    }

    /** Lista de tags detectadas no ultimo frame (singletons e clusters). */
    public List<AprilTagDetection> getDetections() {
        return aprilTag.getDetections();
    }

    /** Pausa o streaming para economizar CPU; use resume() para voltar. */
    public void pause() {
        portal.stopStreaming();
    }

    public void resume() {
        portal.resumeStreaming();
    }

    /** Estado atual da camera (util para saber se ja abriu). */
    public VisionPortal.CameraState getCameraState() {
        return portal.getCameraState();
    }

    /** Fecha a camera. Chamar ao final do OpMode. */
    public void close() {
        if (portal != null) {
            portal.close();
        }
    }
}
