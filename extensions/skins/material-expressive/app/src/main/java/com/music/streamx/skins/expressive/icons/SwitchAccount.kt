/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.music.streamx.skins.expressive

import androidx.compose.ui.graphics.vector.ImageVector

internal val ExpressiveSymbols.SwitchAccount: ImageVector
    get() {
        if (_switchAccount != null) {
            return _switchAccount!!
        }
        _switchAccount = materialIcon(name = "Rounded.SwitchAccount") {
            materialPath {
                moveTo(17.0f, 20.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveTo(2.0f, 6.45f, 2.0f, 7.0f)
                verticalLineToRelative(13.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveTo(17.55f, 20.0f, 17.0f, 20.0f)
                close()
                moveTo(20.0f, 2.0f)
                horizontalLineTo(8.0f)
                curveTo(6.9f, 2.0f, 6.0f, 2.9f, 6.0f, 4.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                curveTo(22.0f, 2.9f, 21.1f, 2.0f, 20.0f, 2.0f)
                close()
                moveTo(14.0f, 5.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, 1.34f, 3.0f, 3.0f)
                curveToRelative(0.0f, 1.66f, -1.34f, 3.0f, -3.0f, 3.0f)
                reflectiveCurveToRelative(-3.0f, -1.34f, -3.0f, -3.0f)
                curveTo(11.0f, 6.34f, 12.34f, 5.0f, 14.0f, 5.0f)
                close()
                moveTo(7.76f, 16.0f)
                curveToRelative(1.47f, -1.83f, 3.71f, -3.0f, 6.24f, -3.0f)
                reflectiveCurveToRelative(4.77f, 1.17f, 6.24f, 3.0f)
                horizontalLineTo(7.76f)
                close()
            }
        }
        return _switchAccount!!
    }

private var _switchAccount: ImageVector? = null
