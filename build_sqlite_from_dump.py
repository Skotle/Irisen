"""Create a local SQLite database from the service_schema section of a MySQL dump.

Usage:
    python build_sqlite_from_dump.py "C:\\path\\export.sql" mydb.db
"""

from __future__ import annotations

import argparse
import re
import sqlite3
from pathlib import Path


def sql_statements(source: str):
    """Split statements while respecting quoted MySQL string literals."""
    start = 0
    quote = None
    escaped = False
    for index, character in enumerate(source):
        if quote:
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == quote:
                quote = None
        elif character in ("'", '"', '`'):
            quote = character
        elif character == ";":
            yield source[start:index + 1]
            start = index + 1


def convert_create(statement: str) -> str:
    """Translate the small MySQL-only subset used by the application schema."""
    body = statement.strip()
    body = re.sub(r"\)\s*ENGINE=.*?;\s*$", ");", body, flags=re.IGNORECASE | re.DOTALL)
    body = re.sub(r"\s+COMMENT\s+'(?:[^'\\]|\\.)*'", "", body, flags=re.IGNORECASE)
    body = re.sub(r"\s+ON UPDATE CURRENT_TIMESTAMP", "", body, flags=re.IGNORECASE)
    body = re.sub(r"\s+AUTO_INCREMENT", "", body, flags=re.IGNORECASE)

    lines = body.splitlines()
    kept = []
    for line in lines:
        stripped = line.strip()
        # MySQL secondary index definitions are not valid inside SQLite CREATE TABLE.
        if re.match(r"(?:UNIQUE\s+)?KEY\s+`", stripped, re.IGNORECASE):
            continue
        kept.append(line)
    body = "\n".join(kept)
    body = re.sub(r",\s*\)", "\n)", body)

    # SQLite needs an INTEGER PRIMARY KEY for generated numeric identifiers.
    primary_key = re.search(r"PRIMARY KEY\s*\(`([^`]+)`\)", body, re.IGNORECASE)
    if primary_key:
        column = re.escape(primary_key.group(1))
        numeric_column = re.compile(
            rf"(`{column}`\s+)(?:bigint|int)(?:\([^)]*\))?\s+NOT NULL(?=\s*[,\n])",
            re.IGNORECASE,
        )
        if numeric_column.search(body):
            body = numeric_column.sub(r"\1INTEGER PRIMARY KEY", body, count=1)
            body = re.sub(
                rf"\s*,?\s*PRIMARY KEY\s*\(`{column}`\)", "", body, count=1,
                flags=re.IGNORECASE,
            )
    return body


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("dump", type=Path)
    parser.add_argument("database", type=Path, nargs="?", default=Path("mydb.db"))
    parser.add_argument("--replace", action="store_true", help="replace an existing target database")
    args = parser.parse_args()

    if args.database.exists() and not args.replace:
        raise SystemExit(f"Refusing to overwrite {args.database}. Use --replace if intended.")

    source = args.dump.read_text(encoding="utf-8")
    marker = "USE `service_schema`;"
    if marker not in source:
        raise SystemExit("service_schema was not found in the dump.")
    source = source.split(marker, 1)[1]

    if args.database.exists():
        args.database.unlink()
    connection = sqlite3.connect(args.database)
    connection.execute("PRAGMA foreign_keys = OFF")
    created = inserted = 0
    try:
        for statement in sql_statements(source):
            normalized = statement.lstrip()
            if normalized.upper().startswith("CREATE TABLE"):
                connection.execute(convert_create(statement))
                created += 1
            elif normalized.upper().startswith("INSERT INTO"):
                # SQLite accepts the dump's quoted values and multi-row INSERT syntax.
                connection.execute(normalized)
                inserted += 1
        connection.commit()
    except sqlite3.Error as error:
        connection.rollback()
        raise SystemExit(f"SQLite import failed after {created} tables and {inserted} inserts: {error}") from error
    finally:
        connection.close()

    print(f"Created {args.database} with {created} tables and {inserted} INSERT statements.")


if __name__ == "__main__":
    main()
