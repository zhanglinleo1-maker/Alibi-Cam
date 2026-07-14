package app.leo.alibi_cam.enums

enum class RecorderState {
    STOPPED,
    RECORDING,
    PAUSED,

    // Only used by the model to indicate that the service is not running
    IDLE
}