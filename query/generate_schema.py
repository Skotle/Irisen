"""Rebuild the browser snapshot from the reviewed relationship document."""
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
document = (ROOT / 'docs/table-relationships.md').read_text(encoding='utf-8')
relations = []
for source, cols, target, target_cols, kind, note in re.findall(
        r'^\| `(\w+)\.\(([^)]+)\)` \| `(\w+)\.\(([^)]+)\)` \| (FK|논리) \| (.*?) \|$', document, re.M):
    relations.append(dict(source=source, columns=cols.split(', '), target=target,
                          targetColumns=target_cols.split(', '), kind=kind, note=note))
tables = []
for name, section in re.findall(r'### (\w+)\n(.*?)(?=\n### |\n## |\Z)', document, re.S):
    columns = []
    for line in section.splitlines():
        if not line.startswith('| `'):
            continue
        cells = [v.strip().replace('`', '') for v in line.strip('|').split('|')]
        columns.append(dict(name=cells[0], type=cells[1], nullable=cells[2], default=cells[3], key=cells[4]))
    group = '콘텐츠' if name.startswith(('post', 'comment')) else '보드' if name.startswith(('board', 'gallery')) else '계정·운영'
    tables.append(dict(name=name, description=section.strip().splitlines()[0], group=group, columns=columns))
lookup = {t['name']: {c['name'] for c in t['columns']} for t in tables}
assert len(tables) == len(lookup) and tables
for r in relations:
    assert set(r['columns']) <= lookup[r['source']]
    assert set(r['targetColumns']) <= lookup[r['target']]
    assert len(r['columns']) == len(r['targetColumns'])
data = dict(date='2026-09-09', tables=tables, relations=relations)
(ROOT / 'query/schema.js').write_text('window.IRISEN_SCHEMA = ' + json.dumps(data, ensure_ascii=False, indent=2) + ';\n', encoding='utf-8')
print(f'{len(tables)} tables, {sum(len(t["columns"]) for t in tables)} columns, {len(relations)} relations')
