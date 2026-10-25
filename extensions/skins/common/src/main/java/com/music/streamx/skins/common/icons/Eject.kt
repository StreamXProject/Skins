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

package com.music.streamx.skins.common.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val SkinIcons.Eject: ImageVector
    get() {
        if (_eject != null) {
            return _eject!!
        }
        _eject = materialIcon(name = "Rounded.Eject") {
            materialPath {
                moveTo(6.0f, 17.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                lineTo(6.0f, 19.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(11.17f, 6.25f)
                lineToRelative(-4.8f, 7.2f)
                curveToRelative(-0.45f, 0.66f, 0.03f, 1.55f, 0.83f, 1.55f)
                horizontalLineToRelative(9.6f)
                curveToRelative(0.8f, 0.0f, 1.28f, -0.89f, 0.83f, -1.55f)
                lineToRelative(-4.8f, -7.2f)
                curveToRelative(-0.39f, -0.6f, -1.27f, -0.6f, -1.66f, 0.0f)
                close()
            }
        }
        return _eject!!
    }

private var _eject: ImageVector? = null
