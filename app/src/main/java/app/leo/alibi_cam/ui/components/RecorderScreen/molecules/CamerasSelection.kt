package app.leo.alibi_cam.ui.components.RecorderScreen.molecules

import android.util.Log
import CameraSelectionButton
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.leo.alibi_cam.R
import app.leo.alibi_cam.ui.models.VideoRecorderModel
import app.leo.alibi_cam.ui.utils.CameraInfo

@Composable
fun CamerasSelection(
    cameras: Iterable<CameraInfo>,
    videoSettings: VideoRecorderModel,
) {
    val hasBackCamera = CameraInfo.getAnyBackCamera(cameras.toList()) != null
    val frontCamera = CameraInfo.getFrontCamera(cameras.toList())

    // Selection state based on cameraLensMode (zoom-driven, not camera-ID-driven)
    val isUWSelected = videoSettings.cameraID == CameraSelector.LENS_FACING_BACK &&
            videoSettings.cameraLensMode == "ultrawide"
    val isMainSelected = videoSettings.cameraID == CameraSelector.LENS_FACING_BACK &&
            videoSettings.cameraLensMode != "ultrawide"
    val isFrontSelected = videoSettings.cameraID == CameraSelector.LENS_FACING_FRONT

    Column {
        // Ultra-wide option — always shown when a back camera exists.
        // UW is engaged via zoom ratio on the logical camera at bind time,
        // not by binding a physical camera ID. If the device lacks UW hardware,
        // zoom stays at 1x and it behaves identically to the main camera.
        if (hasBackCamera) {
            CameraSelectionButton(
                cameraID = CameraInfo.Lens.ULTRA_WIDE,
                label = stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_ultrawide_label),
                selected = isUWSelected,
                onSelected = {
                    videoSettings.cameraID = CameraSelector.LENS_FACING_BACK
                    videoSettings.cameraLensMode = "ultrawide"
                },
            )
        }

        // Main back camera option
        if (hasBackCamera) {
            CameraSelectionButton(
                cameraID = CameraInfo.Lens.BACK,
                label = stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_main_label),
                selected = isMainSelected,
                onSelected = {
                    videoSettings.cameraID = CameraSelector.LENS_FACING_BACK
                    videoSettings.cameraLensMode = "main"
                },
            )
        }

        // Front camera option
        if (frontCamera != null) {
            CameraSelectionButton(
                cameraID = CameraInfo.Lens.FRONT,
                label = stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_front_label),
                selected = isFrontSelected,
                onSelected = {
                    videoSettings.cameraID = CameraSelector.LENS_FACING_FRONT
                    videoSettings.cameraLensMode = null
                },
            )
        }

        // External cameras (if any)
        cameras.forEach { camera ->
            if (camera.lensFacing == CameraSelector.LENS_FACING_EXTERNAL) {
                CameraSelectionButton(
                    cameraID = camera.lens,
                    selected = videoSettings.cameraID == CameraSelector.LENS_FACING_EXTERNAL,
                    onSelected = {
                        videoSettings.cameraID = CameraSelector.LENS_FACING_EXTERNAL
                        videoSettings.cameraLensMode = null
                    },
                    label = stringResource(
                        R.string.ui_videoRecorder_action_start_settings_cameraLens_external_label
                    ),
                )
            }
        }
    }
}
