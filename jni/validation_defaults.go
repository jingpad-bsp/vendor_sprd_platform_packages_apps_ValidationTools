package validation_defaults

import (
        "android/soong/android"
        "android/soong/cc"
        "fmt"
)

func init() {
    // for DEBUG
    fmt.Println("init start")
    android.RegisterModuleType("validation_defaults", validation_defaultsFactory)
}

func validation_defaultsFactory() (android.Module) {
    module := cc.DefaultsFactory()
    android.AddLoadHook(module, validation_defaults)
    return module
}

func validation_defaults(ctx android.LoadHookContext) {
    type props struct {
        Cflags []string
    }
    p := &props{}
    p.Cflags = globalDefaults(ctx)
    ctx.AppendProperties(p)
}

func globalDefaults(ctx android.BaseContext) ([]string) {
    var cppflags []string

    sdkVersion := ctx.AConfig().PlatformSdkVersionInt()
    fmt.Println("sdkVersion:", sdkVersion)

    //Add Dflag USE_AUDIO_WHALE_HAL
    fmt.Println("USE_AUDIO_WHALE_HAL:",
        ctx.AConfig().IsEnvTrue("USE_AUDIO_WHALE_HAL"))
    if ctx.AConfig().IsEnvTrue("USE_AUDIO_WHALE_HAL") {
          cppflags = append(cppflags,
                         "-DAUDIO_WHALE_LOOPBACK=1")
    }

    //Add Dflag TARGET_CAMERA_SENSOR_CCT
    cameraFlagCCT := envDefault(ctx, "TARGET_CAMERA_SENSOR_CCT", "null")
    fmt.Println("TARGET_CAMERA_SENSOR_CCT:",cameraFlagCCT)
    if cameraFlagCCT == "ams_tcs3430"{
        cppflags = append(cppflags,"-DTARGET_CAMERA_SENSOR_CCT_TCS3430")
    }

    //Add Dflag TARGETARCH
    ssss := envDefault(ctx, "PRODUCT_PACKAGES", "null")
    fmt.Println("PRODUCT_PACKAGES:",ssss)

    //Add Dflag TARGET_CAMERA_SENSOR_TOF
    cameraFlagTOF := envDefault(ctx, "TARGET_CAMERA_SENSOR_TOF", "null")
    fmt.Println("TARGET_CAMERA_SENSOR_TOF:",cameraFlagTOF)
    if cameraFlagTOF == "tof_vl53l0"{
        cppflags = append(cppflags,"-DTARGET_CAMERA_SENSOR_TOF_SUPPORT")
    }

    fmt.Println("TARGET_BOARD_SENSOR_OV4C2:",
        ctx.AConfig().IsEnvTrue("TARGET_BOARD_SENSOR_OV4C"))

    //Add Dflag BOARD_FEATUREPHONE_CONFIG
    fmt.Println("BOARD_FEATUREPHONE_CONFIG:",
        ctx.AConfig().IsEnvTrue("BOARD_FEATUREPHONE_CONFIG"))
    if ctx.AConfig().IsEnvTrue("BOARD_FEATUREPHONE_CONFIG") {
          cppflags = append(cppflags,
                         "-DBOARD_FEATUREPHONE_CONFIG")
    }

    return cppflags
}

func envDefault(ctx android.BaseContext, key string, defaultValue string) string {
    ret := ctx.AConfig().Getenv(key)
    fmt.Println("envDefault key:",key)
    fmt.Println("envDefault ret:",ret)
    if ret == "" {
        return defaultValue
    }
    return ret
}