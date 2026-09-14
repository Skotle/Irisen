"""Run the repository's exact profile queries against synthetic in-memory data."""
import pathlib, re, sqlite3
source = pathlib.Path('src/main/java/org/java/spring_04/profile/ProfileService.java').read_text(encoding='utf-8')
db = sqlite3.connect(':memory:')
db.executescript('''
CREATE TABLE board (gall_id TEXT, gall_name TEXT);
CREATE TABLE gallery_setting (gall_id TEXT, visibility TEXT, read_visibility TEXT);
CREATE TABLE user (uid TEXT, nick_type TEXT, nick_icon_type TEXT);
CREATE TABLE post (id INTEGER, gall_id TEXT, post_no INTEGER, title TEXT, writer_uid TEXT,
 writed_at TEXT, view_count INTEGER, recommend_count INTEGER, is_deleted INTEGER,
 is_draft INTEGER, is_secret INTEGER, review_status TEXT);
CREATE TABLE comment (id INTEGER, gall_id TEXT, post_no INTEGER, content TEXT, writer_uid TEXT,
 created_at TEXT, is_deleted INTEGER);
INSERT INTO board VALUES ('audit-private', 'Synthetic private board');
INSERT INTO gallery_setting VALUES ('audit-private', 'private', 'inherit');
INSERT INTO user VALUES ('audit-writer', 'variable', 'default');
INSERT INTO post VALUES (1,'audit-private',1,'SYNTHETIC SECRET DRAFT','audit-writer',
 '2026-09-14',0,0,0,1,1,'review');
INSERT INTO comment VALUES (1,'audit-private',1,'SYNTHETIC PRIVATE COMMENT','audit-writer','2026-09-14',0);
''')
for name in ('getPostsByUser', 'getCommentsByUser'):
    method = source[source.index('private List<Map<String, Object>> '+name):]
    query = re.search(r'"""(.*?)"""', method, re.S).group(1)
    rows = db.execute(query, ('audit-writer',)).fetchall()
    assert len(rows) == 1, (name, rows)
    print(f'CONFIRMED: {name} returns synthetic secret/draft/review content in private board')
