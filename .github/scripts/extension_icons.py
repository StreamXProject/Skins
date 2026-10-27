"""Export each extension's own Android vector as a web/cache-friendly SVG."""
import hashlib
from pathlib import Path
import xml.etree.ElementTree as ET

ANDROID = '{http://schemas.android.com/apk/res/android}'
SVG = 'http://www.w3.org/2000/svg'
ET.register_namespace('', SVG)


def export_icon(root, extension, output):
    main = Path(root) / extension['project'] / 'app/src/main'
    manifest = main / 'AndroidManifest.xml'
    if not manifest.exists():
        return None  # Artifact-only contributors may omit an optional preview.
    application = ET.parse(manifest).find('application')
    resource = application.get(ANDROID + 'icon', '')
    if not resource.startswith(('@drawable/', '@mipmap/')):
        return None
    kind, name = resource[1:].split('/')
    candidates = sorted((main / 'res').glob(f'{kind}*/{name}.xml'), key=lambda p: ('-v' in p.parent.name, len(p.parent.name)))
    vector = next((ET.parse(p).getroot() for p in candidates if ET.parse(p).getroot().tag == 'vector'), None)
    if vector is None:
        return None
    svg = ET.Element('{' + SVG + '}svg', viewBox=f"0 0 {vector.get(ANDROID+'viewportWidth')} {vector.get(ANDROID+'viewportHeight')}")
    defs = ET.SubElement(svg, '{' + SVG + '}defs')

    def color(node, key, value):
        if value.startswith('#') and len(value) == 9:
            node.set(key + '-opacity', str(int(value[1:3], 16) / 255))
            value = '#' + value[3:]
        if value.startswith('@') or value.startswith('?'):
            raise ValueError('Icon colors must be self-contained: ' + resource)
        node.set(key, value)

    def visit(parent, child):
        attrs = {k.removeprefix(ANDROID): v for k, v in child.attrib.items()}
        if child.tag == 'group':
            px, py = attrs.get('pivotX', '0'), attrs.get('pivotY', '0')
            transform = f"translate({attrs.get('translateX','0')} {attrs.get('translateY','0')}) translate({px} {py}) rotate({attrs.get('rotation','0')}) scale({attrs.get('scaleX','1')} {attrs.get('scaleY','1')}) translate({-float(px)} {-float(py)})"
            group = ET.SubElement(parent, '{' + SVG + '}g', transform=transform)
            for item in child: visit(group, item)
        elif child.tag == 'path':
            node = ET.SubElement(parent, '{' + SVG + '}path', d=attrs['pathData'])
            color(node, 'fill', attrs.get('fillColor', '#00000000'))
            for source, target in [('strokeColor','stroke'), ('strokeWidth','stroke-width'), ('strokeLineCap','stroke-linecap'), ('strokeLineJoin','stroke-linejoin'), ('fillAlpha','fill-opacity'), ('strokeAlpha','stroke-opacity')]:
                if source in attrs:
                    if source.endswith('Color'): color(node, target, attrs[source])
                    else: node.set(target, attrs[source])
            if attrs.get('fillType') == 'evenOdd': node.set('fill-rule', 'evenodd')
            for wrapper in child:
                if wrapper.get('name') != 'android:fillColor': continue
                gradient = wrapper.find('gradient')
                if gradient is None: continue
                gid = 'gradient' + str(len(defs))
                g = ET.SubElement(defs, '{' + SVG + '}linearGradient', id=gid, gradientUnits='userSpaceOnUse',
                    x1=gradient.get(ANDROID+'startX','0'), y1=gradient.get(ANDROID+'startY','0'), x2=gradient.get(ANDROID+'endX','0'), y2=gradient.get(ANDROID+'endY','0'))
                stops = list(gradient)
                if not stops:
                    stops = [ET.Element('item', {ANDROID+'offset': '0', ANDROID+'color': gradient.get(ANDROID+'startColor')}), ET.Element('item', {ANDROID+'offset': '1', ANDROID+'color': gradient.get(ANDROID+'endColor')})]
                for item in stops:
                    stop = ET.SubElement(g, '{' + SVG + '}stop', offset=item.get(ANDROID+'offset'))
                    color(stop, 'stop-color', item.get(ANDROID+'color'))
                node.set('fill', f'url(#{gid})')
                if 'fillAlpha' not in attrs: node.attrib.pop('fill-opacity', None)
        else:
            raise ValueError('Unsupported icon vector element: ' + child.tag)

    for child in vector: visit(svg, child)
    data = ET.tostring(svg, encoding='utf-8', xml_declaration=True)
    filename = f"streamx-icon-{extension['packageName']}-{hashlib.sha256(data).hexdigest()[:12]}.svg"
    Path(output).mkdir(parents=True, exist_ok=True)
    (Path(output) / filename).write_bytes(data)
    return filename
