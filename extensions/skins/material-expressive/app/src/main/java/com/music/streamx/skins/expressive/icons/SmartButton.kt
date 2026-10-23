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

internal val ExpressiveSymbols.SmartButton: ImageVector
    get() {
        if (_smartButton != null) {
            return _smartButton!!
        }
        _smartButton = materialIcon(name = "Rounded.SmartButton") {
            materialPath {
                moveTo(22.0f, 9.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineToRelative(-1.0f)
                lineToRelative(0.0f, -2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineTo(9.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(9.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(16.0f)
                curveTo(21.1f, 7.0f, 22.0f, 7.9f, 22.0f, 9.0f)
                close()
                moveTo(14.04f, 17.99f)
                curveToRelative(0.18f, 0.39f, 0.73f, 0.39f, 0.91f, 0.0f)
                lineToRelative(0.63f, -1.4f)
                lineToRelative(1.4f, -0.63f)
                curveToRelative(0.39f, -0.18f, 0.39f, -0.73f, 0.0f, -0.91f)
                lineToRelative(-1.4f, -0.63f)
                lineToRelative(-0.63f, -1.4f)
                curveToRelative(-0.18f, -0.39f, -0.73f, -0.39f, -0.91f, 0.0f)
                lineToRelative(-0.63f, 1.4f)
                lineToRelative(-1.4f, 0.63f)
                curveToRelative(-0.39f, 0.18f, -0.39f, 0.73f, 0.0f, 0.91f)
                lineToRelative(1.4f, 0.63f)
                lineTo(14.04f, 17.99f)
                close()
                moveTo(16.74f, 13.43f)
                curveToRelative(0.1f, 0.22f, 0.42f, 0.22f, 0.52f, 0.0f)
                lineToRelative(0.36f, -0.8f)
                lineToRelative(0.8f, -0.36f)
                curveToRelative(0.22f, -0.1f, 0.22f, -0.42f, 0.0f, -0.52f)
                lineToRelative(-0.8f, -0.36f)
                lineToRelative(-0.36f, -0.8f)
                curveToRelative(-0.1f, -0.22f, -0.42f, -0.22f, -0.52f, 0.0f)
                lineToRelative(-0.36f, 0.8f)
                lineToRelative(-0.8f, 0.36f)
                curveToRelative(-0.22f, 0.1f, -0.22f, 0.42f, 0.0f, 0.52f)
                lineToRelative(0.8f, 0.36f)
                lineTo(16.74f, 13.43f)
                close()
            }
        }
        return _smartButton!!
    }

private var _smartButton: ImageVector? = null
