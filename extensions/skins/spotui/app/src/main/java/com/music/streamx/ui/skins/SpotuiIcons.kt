package com.music.streamx.ui.skins

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp

/** Vector paths ported from the supplied Spotui drawable set.
 * Original Neptune/droidbaza vector assets are Apache-2.0; see extensions/skins/spotui/NOTICE. */
object SpotuiIcons {
    val Home: ImageVector by lazy {
        ImageVector.Builder("spotui_home", 24.dp, 24.dp, 30f, 30f).apply {
            addPath(pathData = addPathNodes("M27.81,29.25H17.8468V19.125H12.1536V29.25H2.1904V8.2054L15.0002,0.75L27.81,8.1546V29.25Z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Search: ImageVector by lazy {
        ImageVector.Builder("spotui_search", 24.dp, 24.dp, 30f, 30f).apply {
            addPath(pathData = addPathNodes("M22.0797,20.3386L28.1623,27.4677L26.0693,29.25L20.0031,22.1373C18.0409,23.4453 15.8771,24.0994 13.5116,24.0994C11.931,24.0994 10.4185,23.7914 8.9742,23.1756C7.5299,22.5597 6.2872,21.7312 5.2461,20.6902C4.205,19.6492 3.3766,18.4065 2.7607,16.9621C2.1449,15.5178 1.837,14.0053 1.8369,12.4247C1.8369,10.8441 2.1448,9.3316 2.7607,7.8873C3.3767,6.4429 4.2051,5.2002 5.2461,4.1592C6.2871,3.1182 7.5298,2.2897 8.9742,1.6738C10.4186,1.0579 11.9311,0.75 13.5116,0.75C15.0922,0.75 16.6046,1.0579 18.049,1.6738C19.4934,2.2897 20.7361,3.1182 21.7771,4.1592C22.8181,5.2002 23.6466,6.4429 24.2625,7.8873C24.8784,9.3316 25.1863,10.8441 25.1863,12.4247C25.1863,13.4276 25.061,14.4086 24.8102,15.3679C24.5595,16.3271 24.2025,17.221 23.7392,18.0495C23.2759,18.878 22.7227,19.641 22.0796,20.3386H22.0797ZM13.5117,21.3523C14.7216,21.3523 15.8771,21.1152 16.9781,20.641C18.0791,20.1668 19.0274,19.5319 19.8232,18.7361C20.6189,17.9404 21.2539,16.992 21.7281,15.8911C22.2023,14.7901 22.4394,13.6346 22.4394,12.4246C22.4394,11.2147 22.2023,10.0592 21.7281,8.9582C21.2539,7.8572 20.6189,6.9088 19.8232,6.1131C19.0274,5.3173 18.0791,4.6823 16.9781,4.2082C15.8771,3.734 14.7216,3.4969 13.5117,3.4969C12.3017,3.4969 11.1462,3.734 10.0452,4.2082C8.9443,4.6823 7.9959,5.3173 7.2001,6.1131C6.4043,6.9089 5.7694,7.8573 5.2952,8.9582C4.8211,10.0591 4.584,11.2146 4.584,12.4246C4.584,13.6347 4.8211,14.7902 5.2952,15.8911C5.7694,16.992 6.4044,17.9403 7.2002,18.7361C7.996,19.5319 8.9444,20.1669 10.0452,20.641C11.1461,21.1151 12.3016,21.3522 13.5117,21.3523Z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Library: ImageVector by lazy {
        ImageVector.Builder("spotui_library", 24.dp, 24.dp, 30f, 30f).apply {
            addPath(pathData = addPathNodes("M17.8046,1.5537L29.25,27.2579L26.559,28.4461L15.1136,2.742L17.8046,1.5537ZM0.75,28.1316V1.711H3.6856V28.1316H0.75ZM8.0891,28.1316V1.711H11.0247V28.1316H8.0892H8.0891Z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Play: ImageVector by lazy {
        ImageVector.Builder("spotui_play", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M8.286,3.407A1.5,1.5 0,0 0,6 4.684v14.632a1.5,1.5 0,0 0,2.286 1.277l11.888,-7.316a1.5,1.5 0,0 0,0 -2.555L8.286,3.407z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Pause: ImageVector by lazy {
        ImageVector.Builder("spotui_pause", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M7,6H9V18H7V6Z"), fill = SolidColor(Color.Black), stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M15,6H17V18H15V6Z"), fill = SolidColor(Color.Black), stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()
    }
    val Next: ImageVector by lazy {
        ImageVector.Builder("spotui_next", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M4,5V19L16,12L4,5Z"), fill = SolidColor(Color.Black), stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M20,5V19"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()
    }
    val Previous: ImageVector by lazy {
        ImageVector.Builder("spotui_previous", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M20,5V19L8,12L20,5Z"), fill = SolidColor(Color.Black), stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M4,5V19"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()
    }
    val Shuffle: ImageVector by lazy {
        ImageVector.Builder("spotui_shuffle", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M11,9C10.5881,8.4246 10.0615,7.9481 9.4561,7.6029C8.8507,7.2578 8.1808,7.0521 7.492,7H3"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M21,7H16.8C13.8,7 12,12 12,12C12,12 10.2,17 7.2,17H3"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M13,15C13.412,15.5761 13.9387,16.053 14.5441,16.3981C15.1495,16.7433 15.8194,16.9486 16.508,17H21"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M18,4L21,7L18,10"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M18,14L21,17L18,20"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()
    }
    val Repeat: ImageVector by lazy {
        ImageVector.Builder("spotui_repeat", 24.dp, 24.dp, 512f, 512f).apply {
            addPath(pathData = addPathNodes("M20,346.91a20,20 0,0 1,-20 -20L0,185.09A102,102 0,0 1,101.89 83.2h268.78l-28.96,-28.96a20,20 0,0 1,28.28 -28.28l63.1,63.1a20,20 0,0 1,0 28.28l-64.97,64.97a20,20 0,1 1,-28.28 -28.28l30.84,-30.83h-268.78A61.96,61.96 0,0 0,40 185.09v141.82a20,20 0,0 1,-20 20zM492,165.09a20,20 0,0 0,-20 20v141.82a61.96,61.96 0,0 1,-61.89 61.89h-268.78l30.83,-30.83a20,20 0,1 0,-28.28 -28.28l-64.97,64.97a20,20 0,0 0,0 28.28l63.1,63.1a20,20 0,0 0,28.28 -28.28L141.33,428.8h268.78A102,102 0,0 0,512 326.91L512,185.09a20,20 0,0 0,-20 -20z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Add: ImageVector by lazy {
        ImageVector.Builder("spotui_add", 24.dp, 24.dp, 512f, 512f).apply {
            addPath(pathData = addPathNodes("M256,0C114.83,0 0,114.83 0,256s114.83,256 256,256 256,-114.85 256,-256S397.17,0 256,0zM256,472.34c-119.28,0 -216.34,-97.05 -216.34,-216.34S136.73,39.66 256,39.66 472.34,136.71 472.34,256 375.3,472.34 256,472.34z"), fill = SolidColor(Color.Black))
            addPath(pathData = addPathNodes("M355.15,234.39H275.83v-79.32c0,-10.95 -8.86,-19.83 -19.83,-19.83s-19.83,8.88 -19.83,19.83v79.32h-79.32c-10.97,0 -19.83,8.88 -19.83,19.83s8.86,19.83 19.83,19.83h79.32v79.32c0,10.95 8.86,19.83 19.83,19.83s19.83,-8.88 19.83,-19.83v-79.32h79.32c10.97,0 19.83,-8.88 19.83,-19.83s-8.86,-19.83 -19.83,-19.83z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Check: ImageVector by lazy {
        ImageVector.Builder("spotui_check", 24.dp, 24.dp, 408.58f, 408.58f).apply {
            addPath(pathData = addPathNodes("M204.29,0C91.65,0 0,91.65 0,204.29s91.65,204.29 204.29,204.29 204.29,-91.65 204.29,-204.29S316.93,0 204.29,0zM318.46,150.53 L187.9,280.06c-7.68,7.68 -19.97,8.19 -28.16,0.51L90.62,217.6c-8.19,-7.68 -8.7,-20.48 -1.54,-28.67 7.68,-8.19 20.48,-8.7 28.67,-1.02l54.78,50.18L289.28,121.34c8.19,-8.19 20.99,-8.19 29.18,0s8.19,20.99 0,29.18z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Down: ImageVector by lazy {
        ImageVector.Builder("spotui_down", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M6,9L12,15L18,9"), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }.build()
    }
    val More: ImageVector by lazy {
        ImageVector.Builder("spotui_more", 24.dp, 24.dp, 32f, 32f).apply {
            addPath(pathData = addPathNodes("M16,10c1.7,0 3,-1.3 3,-3s-1.3,-3 -3,-3s-3,1.3 -3,3S14.3,10 16,10z"), fill = SolidColor(Color.Black))
            addPath(pathData = addPathNodes("M16,13c-1.7,0 -3,1.3 -3,3s1.3,3 3,3s3,-1.3 3,-3S17.7,13 16,13z"), fill = SolidColor(Color.Black))
            addPath(pathData = addPathNodes("M16,22c-1.7,0 -3,1.3 -3,3s1.3,3 3,3s3,-1.3 3,-3S17.7,22 16,22z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Download: ImageVector by lazy {
        ImageVector.Builder("spotui_download", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M5,20h14v-2H5v2zM19,9h-4V3H9v6H5l7,7 7,-7z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Devices: ImageVector by lazy {
        ImageVector.Builder("spotui_devices", 24.dp, 24.dp, 512f, 512f).apply {
            addPath(pathData = addPathNodes("M478.32,125.94L478.32,72.5c0,-25.21 -20.51,-45.71 -45.71,-45.71h-386.9C20.51,26.78 0,47.29 0,72.5L0,308.06c0,25.21 20.51,45.71 45.71,45.71h107.12l-5.93,48.03h-26.56c-23,0 -41.71,18.71 -41.71,41.71 0,23 18.71,41.71 41.71,41.71h343.7c26.44,0 47.95,-21.51 47.95,-47.95L512,171.72c0,-21.47 -14.19,-39.69 -33.68,-45.78zM32,308.06L32,72.5c0,-7.56 6.15,-13.71 13.71,-13.71h386.9c7.56,0 13.71,6.15 13.71,13.71v51.27L339.2,123.77c-26.44,0 -47.95,21.51 -47.95,47.95v150.05L45.71,321.77C38.15,321.77 32,315.62 32,308.06zM185.08,353.77h106.17v48.03L179.15,401.8zM110.64,443.51c0,-5.26 4.45,-9.71 9.71,-9.71h170.91v3.46c0,5.59 0.97,10.96 2.74,15.95L120.34,453.22c-5.26,0 -9.71,-4.45 -9.71,-9.71zM480,437.26c0,8.8 -7.16,15.95 -15.95,15.95L339.2,453.22c-8.8,0 -15.95,-7.16 -15.95,-15.95L323.25,171.72c0,-8.8 7.16,-15.95 15.95,-15.95h124.84c8.8,0 15.95,7.16 15.95,15.95zM424.87,406.81c0,12.8 -10.44,23.25 -23.25,23.25s-23.25,-10.44 -23.25,-23.25 10.44,-23.25 23.25,-23.25 23.25,10.44 23.25,23.25z"), fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd)
        }.build()
    }
    val Settings: ImageVector by lazy {
        ImageVector.Builder("spotui_settings", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Share: ImageVector by lazy {
        ImageVector.Builder("spotui_share", 24.dp, 24.dp, 227.22f, 227.22f).apply {
            addPath(pathData = addPathNodes("M175.9,141.48c-13.25,0 -25.11,6.04 -32.98,15.52l-51.19,-29.07a42.67,42.67 0,0 0,2.47 -14.32,42.67 42.67,0 0,0 -2.47,-14.32l51.19,-29.07c7.87,9.48 19.73,15.52 32.98,15.52 23.63,0 42.86,-19.24 42.86,-42.88C218.76,19.23 199.53,0 175.9,0 152.26,0 133.03,19.23 133.03,42.87c0,5.02 0.87,9.84 2.47,14.32L84.3,86.26c-7.87,-9.47 -19.73,-15.51 -32.97,-15.51 -23.64,0 -42.87,19.23 -42.87,42.87 0,23.64 19.23,42.87 42.87,42.87 13.25,0 25.1,-6.04 32.97,-15.51l51.19,29.07a42.67,42.67 0,0 0,-2.47 14.32c0,23.64 19.23,42.87 42.87,42.87 23.63,0 42.86,-19.23 42.86,-42.87 0,-23.64 -19.23,-42.87 -42.86,-42.87zM175.9,15c15.36,0 27.86,12.5 27.86,27.86 0,15.37 -12.5,27.88 -27.86,27.88 -15.37,0 -27.87,-12.51 -27.87,-27.88C148.03,27.5 160.53,15 175.9,15zM51.33,141.48c-15.37,0 -27.87,-12.5 -27.87,-27.86 0,-15.37 12.5,-27.87 27.87,-27.87 15.36,0 27.86,12.5 27.86,27.87 0,15.36 -12.5,27.86 -27.86,27.86zM175.9,212.22c-15.37,0 -27.87,-12.5 -27.87,-27.86 0,-15.37 12.5,-27.88 27.87,-27.88 15.36,0 27.86,12.51 27.86,27.88 0,15.36 -12.5,27.86 -27.86,27.86z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Grid: ImageVector by lazy {
        ImageVector.Builder("spotui_grid", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M4,4h7v7H4zM13,4h7v7h-7zM4,13h7v7H4zM13,13h7v7h-7z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val List: ImageVector by lazy {
        ImageVector.Builder("spotui_list", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M4,5h2v2H4zM8,5h12v2H8zM4,11h2v2H4zM8,11h12v2H8zM4,17h2v2H4zM8,17h12v2H8z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Translate: ImageVector by lazy {
        ImageVector.Builder("spotui_translate", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M8.155,1.012Q8.571,1.012 8.863,1.304Q9.155,1.595 9.155,2.012L9.155,3.583L14.167,3.583Q14.583,3.583 14.875,3.875Q15.167,4.167 15.167,4.577Q15.167,4.988 14.875,5.286Q14.583,5.583 14.167,5.583L12.202,5.583Q12.155,7.429 11.482,9.161Q10.81,10.893 9.607,12.298Q10.536,13.036 11.643,13.476Q12.75,13.917 13.94,14.012L15.774,9.81Q15.893,9.536 16.143,9.369Q16.393,9.202 16.69,9.202Q16.988,9.202 17.238,9.369Q17.488,9.536 17.607,9.81L22.762,21.607Q22.929,21.988 22.774,22.375Q22.619,22.762 22.244,22.923Q21.869,23.083 21.482,22.935Q21.095,22.786 20.929,22.417L19.512,19.167L13.869,19.167L12.452,22.417Q12.345,22.667 12.131,22.833Q11.917,23 11.643,23.03Q11.369,23.06 11.125,22.952Q10.881,22.845 10.72,22.625Q10.56,22.405 10.53,22.131Q10.5,21.857 10.619,21.607L13.095,15.929Q11.738,15.714 10.476,15.149Q9.214,14.583 8.155,13.69Q6.893,14.702 5.393,15.298Q3.893,15.893 2.286,16.012Q1.869,16.048 1.554,15.78Q1.238,15.512 1.208,15.095Q1.179,14.679 1.446,14.363Q1.714,14.048 2.131,14.024Q3.381,13.929 4.56,13.476Q5.738,13.024 6.738,12.262Q5.333,10.524 4.786,8.357Q4.726,8.095 4.798,7.833Q4.869,7.571 5.06,7.387Q5.25,7.202 5.512,7.137Q5.774,7.071 6.036,7.143Q6.298,7.214 6.482,7.411Q6.667,7.607 6.726,7.869Q7.143,9.524 8.19,10.869Q9.119,9.75 9.637,8.393Q10.155,7.036 10.19,5.583L2.155,5.583Q1.738,5.583 1.446,5.286Q1.155,4.988 1.155,4.577Q1.155,4.167 1.446,3.875Q1.738,3.583 2.155,3.583L7.155,3.583L7.155,2.012Q7.155,1.595 7.452,1.304Q7.75,1.012 8.155,1.012ZM14.75,17.167L18.643,17.167L16.69,12.702Z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val Visibility: ImageVector by lazy {
        ImageVector.Builder("spotui_visibility", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73,-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z"), fill = SolidColor(Color.Black))
        }.build()
    }
    val VisibilityOff: ImageVector by lazy {
        ImageVector.Builder("spotui_visibilityoff", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M12,7c2.76,0 5,2.24 5,5 0,0.65 -0.13,1.26 -0.36,1.83l2.92,2.92c1.51,-1.26 2.7,-2.89 3.43,-4.75 -1.73,-4.39 -6,-7.5 -11,-7.5 -1.4,0 -2.74,0.25 -3.98,0.7l2.16,2.16C10.74,7.13 11.35,7 12,7zM2,4.27l2.28,2.28 0.46,0.46C3.08,8.3 1.78,10.02 1,12c1.73,4.39 6,7.5 11,7.5 1.55,0 3.03,-0.3 4.38,-0.84l0.42,0.42L19.73,22 21,20.73 3.27,3 2,4.27zM7.53,9.8l1.55,1.55c-0.05,0.21 -0.08,0.43 -0.08,0.65 0,1.66 1.34,3 3,3 0.22,0 0.44,-0.03 0.65,-0.08l1.55,1.55c-0.67,0.33 -1.41,0.53 -2.2,0.53 -2.76,0 -5,-2.24 -5,-5 0,-0.79 0.2,-1.53 0.53,-2.2zM11.84,9.02l3.15,3.15 0.02,-0.16c0,-1.66 -1.34,-3 -3,-3l-0.17,0.01z"), fill = SolidColor(Color.Black))
        }.build()
    }
}
