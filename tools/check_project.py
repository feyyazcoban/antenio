from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
required = [
    "README.md", "AGENTS.md", "CLAUDE.md", "docs/product/PRD.md",
    "docs/design/FIGMA.md", "docs/design/SCREENS.md", "docs/design/MOTION.md",
    "docs/design/COLOR.md", "docs/research/SOURCE_GUIDELINES.md",
    "docs/research/RESEARCH_PLAN.md", "docs/project/STATUS.md",
    "docs/project/HANDOFF.md", "docs/project/BACKLOG.md", "docs/project/WORKFLOW.md",
    "docs/decisions/DECISIONS.md", "design/figma/manifest.json",
    "design/tokens/figma-variables.snapshot.json",
]
errors = [f"Missing: {name}" for name in required if not (ROOT / name).is_file()]
for file in ROOT.rglob("*.md"):
    text = file.read_text(encoding="utf-8")
    for target in re.findall(r"\]\(([^)]+)\)", text):
        if "://" in target or target.startswith("#"):
            continue
        target = target.split("#", 1)[0]
        if target and not (file.parent / target).exists():
            errors.append(f"Broken link: {file.relative_to(ROOT)} -> {target}")
manifest = json.loads((ROOT / "design/figma/manifest.json").read_text(encoding="utf-8"))
ids = [s["id"] for s in manifest["screens"]]
if len(ids) != len(set(ids)):
    errors.append("Duplicate screen IDs")
screen_text = (ROOT / "docs/design/SCREENS.md").read_text(encoding="utf-8")
for sid in ids:
    if sid not in screen_text:
        errors.append(f"Undocumented screen: {sid}")
snapshot = json.loads((ROOT / "design/tokens/figma-variables.snapshot.json").read_text(encoding="utf-8"))
var_ids = {v["id"] for v in snapshot["variables"]}
for variable in snapshot["variables"]:
    for val in variable["valuesByMode"].values():
        if isinstance(val, dict) and val.get("type") == "VARIABLE_ALIAS" and val["id"] not in var_ids:
            errors.append(f"Unresolved alias: {variable['name']}")
prd = (ROOT / "docs/product/PRD.md").read_text(encoding="utf-8")
if re.search(r"\bV0\b", prd):
    errors.append("Obsolete optional V0 scope")
if errors:
    raise SystemExit("\n".join(errors))
print(f"PASS: local links, {len(ids)} screens, {len(var_ids)} variables, required documents")
print("Figma pending tasks:", len(manifest.get("pending", [])))
