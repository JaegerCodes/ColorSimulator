package com.llamasoft.envi.util

enum class ProviderType {
    BASIC,
    GOOGLE,
    FACEBOOK
}

enum class AuthType {
    SIGNIN,
    SIGNUP,
}

enum class BundleName(val value: String) {
    LoadImage("loadImage"),
    UriImage("uriImage"),
    ShowNextStepButton("showNextStepButton"),
    IsRoot("isRoot")
}

enum class LoadImage {
    Local,
    Remote;
    companion object {
        fun get(name: String?) = valueOf(name?: Local.name)
    }
}


enum class ProjectSteps {
    StartProject,
    ProjectMenu,
    ImagePainter;
    companion object {
        fun get(orindal: Int?) = enumValues<ProjectSteps>()[orindal?:0]
    }
}

enum class ColorScannerSteps {
    StartColors;
    companion object {
        fun get(orindal: Int?) = enumValues<ColorScannerSteps>()[orindal?:0]
    }
}

enum class PaintMenu {
    Bucket,
    Brush,
    Mirror,
    //Eraser,
    Undo,
    //Redo,
    Swatch
}

enum class PaintType {
    Paint,
    Brush,
    None
}