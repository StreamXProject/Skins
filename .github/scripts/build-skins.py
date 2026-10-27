#!/usr/bin/env python3
"""Build every skin in extensions.json and stage its APKs as skin-artifacts/<id>/."""
import json
import os
from pathlib import Path
import shutil
import subprocess

root = Path(__file__).resolve().parents[2]
skins = [item for item in json.loads((root / 'extensions.json').read_text()) if item['type'] == 'skin']
command = ['./gradlew', '--no-daemon']
if os.environ.get('EXTENSION_BUILD_NUMBER'):
    command.append('-PextensionBuildNumber=' + os.environ['EXTENSION_BUILD_NUMBER'])
command += [f':{skin["module"]}:assembleRelease' for skin in skins]
subprocess.run(command, cwd=root, check=True)
for skin in skins:
    apks = list((root / skin['project'] / 'app/build/outputs/apk/release').glob('*.apk'))
    if not apks:
        raise SystemExit('No release APK for ' + skin['id'])
    destination = root / 'skin-artifacts' / skin['id']
    destination.mkdir(parents=True, exist_ok=True)
    for apk in apks:
        shutil.copy2(apk, destination / apk.name)
print(f'Staged APKs for {len(skins)} skins')
