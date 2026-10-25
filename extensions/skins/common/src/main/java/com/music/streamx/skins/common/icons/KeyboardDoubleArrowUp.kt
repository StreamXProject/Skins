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

public val SkinIcons.KeyboardDoubleArrowUp: ImageVector
    get() {
        if (_keyboardDoubleArrowUp != null) {
            return _keyboardDoubleArrowUp!!
        }
        _keyboardDoubleArrowUp = materialIcon(name = "Rounded.KeyboardDoubleArrowUp") {
            materialPath {
                moveTo(6.7f, 18.29f)
                lineTo(6.7f, 18.29f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(12.0f, 14.42f)
                lineToRelative(3.88f, 3.88f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-4.59f, -4.59f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(6.7f, 16.88f)
                curveTo(6.31f, 17.27f, 6.31f, 17.9f, 6.7f, 18.29f)
                close()
            }
            materialPath {
                moveTo(6.7f, 11.7f)
                lineTo(6.7f, 11.7f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(12.0f, 7.83f)
                lineToRelative(3.88f, 3.88f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-4.59f, -4.59f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(6.7f, 10.29f)
                curveTo(6.31f, 10.68f, 6.31f, 11.31f, 6.7f, 11.7f)
                close()
            }
        }
        return _keyboardDoubleArrowUp!!
    }

private var _keyboardDoubleArrowUp: ImageVector? = null
