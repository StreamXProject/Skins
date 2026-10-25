# skinCommon

Shared chrome for the executable skins under `extensions/skins/`: a `BaseSkinRenderer`
that implements every `SkinUiRenderer` slot (mini player, headers, navigation, library
rows, feed shelves, sleep timer, lyrics and queue pages, host sheets) so a skin only has
to draw its now-playing stage. Bundled into each skin APK as an `implementation`
dependency; it is not a host module.
