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

internal val ExpressiveSymbols.Upgrade: ImageVector
    get() {
        if (_upgrade != null) {
            return _upgrade!!
        }
        _upgrade = materialIcon(name = "Rounded.Upgrade") {
            materialPath {
                moveTo(16.0f, 19.0f)
                lineTo(16.0f, 19.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveTo(15.55f, 18.0f, 16.0f, 18.45f, 16.0f, 19.0f)
                close()
                moveTo(11.0f, 7.99f)
                verticalLineTo(15.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(7.99f)
                horizontalLineToRelative(1.79f)
                curveToRelative(0.45f, 0.0f, 0.67f, -0.54f, 0.35f, -0.85f)
                lineToRelative(-2.79f, -2.78f)
                curveToRelative(-0.2f, -0.19f, -0.51f, -0.19f, -0.71f, 0.0f)
                lineTo(8.86f, 7.14f)
                curveTo(8.54f, 7.45f, 8.76f, 7.99f, 9.21f, 7.99f)
                horizontalLineTo(11.0f)
                close()
            }
        }
        return _upgrade!!
    }

private var _upgrade: ImageVector? = null
