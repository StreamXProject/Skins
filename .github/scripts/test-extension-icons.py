import importlib.util
from pathlib import Path
import tempfile
import unittest
import xml.etree.ElementTree as ET

spec = importlib.util.spec_from_file_location('extension_icons', Path(__file__).with_name('extension_icons.py'))
icons = importlib.util.module_from_spec(spec)
spec.loader.exec_module(icons)

class IconTests(unittest.TestCase):
    def test_exports_extension_owned_vector_with_visible_gradient(self):
        with tempfile.TemporaryDirectory() as temp:
            root=Path(temp)
            main=root/'demo/app/src/main'
            (main/'res/drawable').mkdir(parents=True)
            (main/'AndroidManifest.xml').write_text('<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application android:icon="@drawable/logo" /></manifest>')
            (main/'res/drawable/logo.xml').write_text('''<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt" android:viewportWidth="24" android:viewportHeight="24"><path android:pathData="M0,0H24V24H0Z"><aapt:attr name="android:fillColor"><gradient android:startColor="#FF0000" android:endColor="#0000FF" android:endY="24" /></aapt:attr></path></vector>''')
            extension={'project':'demo','packageName':'com.example.demo'}
            name=icons.export_icon(root,extension,root/'out')
            self.assertEqual(name,icons.export_icon(root,extension,root/'out'))
            svg=ET.parse(root/'out'/name)
            path=svg.find('{http://www.w3.org/2000/svg}path')
            self.assertTrue(path.get('fill').startswith('url('))
            self.assertNotEqual('0.0',path.get('fill-opacity'))
            self.assertEqual(2,len(svg.findall('.//{http://www.w3.org/2000/svg}stop')))
    def test_missing_source_does_not_block_artifact_only_catalogues(self):
        with tempfile.TemporaryDirectory() as temp:
            self.assertIsNone(icons.export_icon(temp,{'project':'missing','packageName':'com.example.demo'},Path(temp)/'out'))

if __name__=='__main__': unittest.main()
