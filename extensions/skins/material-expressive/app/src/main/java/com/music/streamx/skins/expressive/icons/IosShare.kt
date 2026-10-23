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

internal val ExpressiveSymbols.IosShare: ImageVector
    get() {
        if (_iosShare != null) {
            return _iosShare!!
        }
        _iosShare = materialIcon(name = "Rounded.IosShare") {
            materialPath {
                moveTo(18.0f, 8.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(11.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(10.0f)
                curveTo(20.0f, 8.9f, 19.1f, 8.0f, 18.0f, 8.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 16.0f)
                lineTo(12.0f, 16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(1.79f)
                curveToRelative(0.45f, 0.0f, 0.67f, -0.54f, 0.35f, -0.85f)
                lineToRelative(-2.79f, -2.79f)
                curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
                lineTo(8.85f, 4.15f)
                curveTo(8.54f, 4.46f, 8.76f, 5.0f, 9.21f, 5.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(10.0f)
                curveTo(11.0f, 15.55f, 11.45f, 16.0f, 12.0f, 16.0f)
                close()
            }
        }
        return _iosShare!!
    }

private var _iosShare: ImageVector? = null
