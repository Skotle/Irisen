# 메모리 DB 즉시 실행

Windows PowerShell에서 `./run-memory.cmd`, Linux/macOS에서 `bash ./run-memory.sh`를 실행한다.
Java 23 이상이 필요하며 Maven wrapper가 애플리케이션을 빌드한다. 첫 빌드에는 의존성 다운로드가 필요할 수 있다.
브라우저 주소는 `http://127.0.0.1:8080`이다.

외부 DB 접속이나 DB/SMTP 자격증명 입력 없이, JVM 프로세스 안의 SQLite 엔진에 빈 39개 테이블을 생성한다.
SQLite는 Java 힙 객체가 아닌 JDBC 드라이버의 네이티브 메모리를 사용한다.
테이블 정의는 `src/main/resources/schema-memory.sql`에 포함되어 있어 실행 시 `mydb.db`나 Python이 필요 없다.
기존 데이터, 계정, 게시판을 복사하거나 자동 생성하지 않는다. 기존 SQLite 모드의 기능 제약은 그대로 적용된다.

실행 콘솔에서 `stats`로 테이블 건수를 확인하고, `exit` 또는 `종료`로 저장 후 종료한다.
정상적인 JVM 종료 훅이 실행되는 Ctrl+C 종료도 저장한다. 강제 종료, 전원 손실, JVM 충돌에서는 저장할 수 없다.
기본 저장 위치는 `snapshots/irisen-<시각>-<UUID>.sqlite`이며 시작/종료 로그에 절대 경로가 표시된다.
저장 실패는 `[MEMORY DB] EXPORT FAILED`로 표시되므로 종료 로그를 확인한다.

저장 경로를 지정하려면 `./run-memory.cmd "C:\temp\session.sqlite"` 또는
`bash ./run-memory.sh /tmp/session.sqlite`를 사용한다. `APP_SQLITE_EXPORT_PATH`로도 지정할 수 있다.
이미 존재하는 파일을 지정하면 시작을 거부한다. 매 실행은 새 빈 DB로 시작하며 저장된 파일을 자동 복원하지 않는다.

기존 `env set/get/unset` 명령은 그대로 사용할 수 있지만, DB 모드와 저장 경로는 시작 시 결정된다.
실행 중 `env set`으로 변경하거나 재시작 후 값을 유지할 수 없다.
DB 외의 이메일 전송·이미지 업로드 등 외부 서비스 기능은 별도 설정이 필요하다.
