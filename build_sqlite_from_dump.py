from __future__ import annotations

import argparse
import re
import sqlite3
from pathlib import Path


ROOT = Path(__file__).resolve().parent
DEFAULT_DUMP = ROOT / "Cloud_SQL_Export_2026-06-11.19_07_44 (1).sql"
DEFAULT_DB = ROOT / "mydb.db"


def strip_mysql_comments(sql: str) -> str:
    sql = re.sub(r"/\*!\d+.*?\*/\s*;?", "", sql, flags=re.S)
    lines: list[str] = []
    for line in sql.splitlines():
        stripped = line.lstrip()
        if stripped.startswith("--") or stripped.startswith("#"):
            continue
        lines.append(line)
    return "\n".join(lines)


def split_statements(sql: str) -> list[str]:
    statements: list[str] = []
    buf: list[str] = []
    in_single = False
    escape = False
    for ch in sql:
        if in_single:
            buf.append(ch)
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == "'":
                in_single = False
        else:
            if ch == "'":
                in_single = True
                buf.append(ch)
            elif ch == ";":
                stmt = "".join(buf).strip()
                if stmt:
                    statements.append(stmt)
                buf = []
            else:
                buf.append(ch)
    tail = "".join(buf).strip()
    if tail:
        statements.append(tail)
    return statements


def normalize_identifier_list(text: str) -> str:
    return text.replace("`", "")


def clean_column_line(line: str) -> str:
    line = line.strip().rstrip(",")
    line = line.replace("`", "")
    line = re.sub(r"\s+COMMENT\s+'(?:[^'\\]|\\.)*'", "", line, flags=re.I)
    line = re.sub(r"\s+COMMENT\s*=\s*'(?:[^'\\]|\\.)*'", "", line, flags=re.I)
    line = re.sub(r"\s+COLLATE\s+\w+", "", line, flags=re.I)
    line = re.sub(r"\s+CHARACTER SET\s+\w+", "", line, flags=re.I)
    line = re.sub(r"\s+UNSIGNED", "", line, flags=re.I)
    line = re.sub(r"\s+AUTO_INCREMENT", "", line, flags=re.I)
    line = re.sub(r"\s+ON UPDATE CURRENT_TIMESTAMP", "", line, flags=re.I)
    line = re.sub(r"\s+DEFAULT CHARSET\s*=\s*\w+", "", line, flags=re.I)
    line = re.sub(r"\s+ENGINE\s*=\s*\w+", "", line, flags=re.I)
    line = re.sub(r"\s+DEFAULT COLLATE\s*=\s*[\w_]+", "", line, flags=re.I)
    line = re.sub(r"\s+COLLATE\s*=\s*[\w_]+", "", line, flags=re.I)
    line = re.sub(r"\s+USING\s+\w+", "", line, flags=re.I)
    line = re.sub(r"\s+KEY_BLOCK_SIZE\s*=\s*\d+", "", line, flags=re.I)
    line = re.sub(r"\s+COMMENT\s*=\s*'[^']*'", "", line, flags=re.I)
    return line.strip()


def translate_create_table(stmt: str) -> tuple[str, list[str]]:
    match = re.match(
        r"(?is)^CREATE TABLE\s+`?(?P<name>[^`(]+)`?\s*\((?P<body>.*)\)\s*ENGINE=.*$",
        stmt.strip(),
    )
    if not match:
        raise ValueError(f"Unrecognized CREATE TABLE statement:\n{stmt[:500]}")

    table_name = match.group("name").strip()
    body = match.group("body")
    raw_lines = [line.strip() for line in body.splitlines() if line.strip()]

    processed: list[str] = []
    indexes: list[str] = []
    autoinc_column: str | None = None
    autoinc_line_index: int | None = None
    primary_key_single_column: str | None = None
    primary_key_line_index: int | None = None

    for idx, raw_line in enumerate(raw_lines):
        line = clean_column_line(raw_line)
        if not line:
            continue

        upper = line.upper()
        if "AUTO_INCREMENT" in raw_line.upper():
            col_match = re.match(r"^([A-Za-z0-9_]+)\s+", line)
            if col_match:
                autoinc_column = col_match.group(1)
                autoinc_line_index = len(processed)

        pk_match = re.match(r"(?is)^PRIMARY KEY\s*\(\s*([A-Za-z0-9_]+)\s*\)$", line)
        if pk_match:
            primary_key_single_column = pk_match.group(1)
            primary_key_line_index = len(processed)
            processed.append(line)
            continue

        unique_match = re.match(
            r"(?is)^UNIQUE KEY\s+([A-Za-z0-9_]+)\s*\((.*)\)$",
            line,
        )
        if unique_match:
            cols = normalize_identifier_list(unique_match.group(2).strip())
            processed.append(f"UNIQUE ({cols})")
            continue

        key_match = re.match(r"(?is)^KEY\s+([A-Za-z0-9_]+)\s*\((.*)\)$", line)
        if key_match:
            key_name = key_match.group(1)
            cols = normalize_identifier_list(key_match.group(2).strip())
            index_name = f"idx_{table_name}_{key_name}"
            indexes.append(f'CREATE INDEX IF NOT EXISTS {index_name} ON {table_name} ({cols})')
            continue

        constraint_match = re.match(r"(?is)^CONSTRAINT\s+", line)
        if constraint_match:
            processed.append(normalize_identifier_list(line))
            continue

        processed.append(normalize_identifier_list(line))

    if autoinc_column and primary_key_single_column == autoinc_column:
        if autoinc_line_index is not None:
            col_line = processed[autoinc_line_index]
            col_line = re.sub(r"(?is)\s+NOT NULL\s*$", "", col_line)
            col_line = re.sub(r"(?is)^[A-Za-z0-9_]+\s+\w+(?:\(\d+\))?", f"{autoinc_column} INTEGER PRIMARY KEY AUTOINCREMENT", col_line)
            processed[autoinc_line_index] = col_line
        if primary_key_line_index is not None and primary_key_line_index < len(processed):
            processed[primary_key_line_index] = None  # type: ignore[assignment]

    cleaned_lines = [line for line in processed if line]
    body_sql = ",\n  ".join(cleaned_lines)
    table_sql = f"CREATE TABLE {table_name} (\n  {body_sql}\n)"
    return table_sql, indexes


def build_sqlite(dump_path: Path, db_path: Path) -> None:
    raw_sql = dump_path.read_text(encoding="utf-8", errors="ignore")
    raw_sql = strip_mysql_comments(raw_sql)
    statements = split_statements(raw_sql)

    create_tables: list[str] = []
    create_indexes: list[str] = []
    inserts: list[str] = []

    for stmt in statements:
        stripped = stmt.strip()
        if not stripped:
            continue
        upper = stripped.upper()
        if upper.startswith("CREATE DATABASE") or upper.startswith("USE ") or upper.startswith("LOCK TABLES") or upper.startswith("UNLOCK TABLES"):
            continue
        if upper.startswith("SET ") or upper.startswith("START TRANSACTION"):
            continue
        if upper.startswith("DROP TABLE"):
            continue
        if upper.startswith("CREATE TABLE"):
            table_sql, index_sqls = translate_create_table(stripped)
            create_tables.append(table_sql)
            create_indexes.extend(index_sqls)
            continue
        if upper.startswith("INSERT INTO"):
            inserts.append(normalize_identifier_list(stripped))
            continue

    if db_path.exists():
        db_path.unlink()

    conn = sqlite3.connect(db_path)
    try:
        conn.execute("PRAGMA foreign_keys=OFF")
        conn.executescript("\n;\n".join(create_tables) + ";\n")
        if create_indexes:
            conn.executescript("\n;\n".join(create_indexes) + ";\n")
        if inserts:
            conn.executescript("\n;\n".join(inserts) + ";\n")
        conn.commit()
    finally:
        conn.close()


def main() -> None:
    parser = argparse.ArgumentParser(description="Build a SQLite database from the MySQL dump.")
    parser.add_argument("--dump", type=Path, default=DEFAULT_DUMP)
    parser.add_argument("--output", type=Path, default=DEFAULT_DB)
    args = parser.parse_args()

    if not args.dump.exists():
        raise SystemExit(f"Dump not found: {args.dump}")

    build_sqlite(args.dump, args.output)
    print(f"Built SQLite database at {args.output}")


if __name__ == "__main__":
    main()
