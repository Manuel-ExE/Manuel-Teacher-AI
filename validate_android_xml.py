from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).parent
files = sorted((root / "app" / "src" / "main" / "res").rglob("*.xml"))
for path in files:
    ET.parse(path)
print(f"Parsed {len(files)} Android XML files successfully")
manifest = ET.parse(root / "app" / "src" / "main" / "AndroidManifest.xml")
activities = {node.attrib.get("{http://schemas.android.com/apk/res/android}name") for node in manifest.getroot().find("application") if node.tag == "activity"}
expected = {".ui.WorkspaceToolActivity", ".ui.StudentRecordsActivity", ".ui.MaterialsActivity", ".ui.AssistantActivity"}
missing = expected - activities
if missing:
    raise SystemExit(f"Missing manifest activities: {sorted(missing)}")
print("All new activities are registered")
