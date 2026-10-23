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

package com.music.streamx.skins.material

import androidx.compose.ui.graphics.vector.ImageVector

internal val ExpressiveSymbols.NorthWest: ImageVector
    get() {
        if (_northWest != null) {
            return _northWest!!
        }
        _northWest = materialIcon(name = "Rounded.NorthWest") {
            materialPath {
                moveTo(6.0f, 15.0f)
                lineTo(6.0f, 15.0f)
                curveToRelative(0.56f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(8.41f)
                lineTo(17.89f, 19.3f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(8.41f, 7.0f)
                horizontalLineTo(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(6.0f)
                curveTo(5.45f, 5.0f, 5.0f, 5.45f, 5.0f, 6.0f)
                verticalLineTo(14.0f)
                curveTo(5.0f, 14.55f, 5.45f, 15.0f, 6.0f, 15.0f)
                close()
            }
        }
        return _northWest!!
    }

private var _northWest: ImageVector? = null
