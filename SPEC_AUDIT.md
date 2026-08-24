# ManuelTAi specification audit

This audit compares the attached teacher-workspace specification with the current Android project.

| Specification area | Current state | Gap to close |
|---|---|---|
| Android-only, offline-first | Present | Preserve the no-background/no-cloud constraint |
| Dashboard with local/offline status | Present | Add entry points for every requested tool |
| AI Teacher Assistant conversation | Missing | Add a local chat screen using the same on-device engine and local retrieval |
| Lesson Planner | Present | Add local-material context and save/export support |
| Question Generator | Present | Add edit/save/export/print-oriented actions and local context |
| Worksheet Creator | Present in latest workspace branch | Add student-name/instructions/answer-key controls and export |
| Quiz Creator | Present in latest workspace branch | Add objective answer key and export |
| Marking Assistant | Present in latest workspace branch | Keep teacher in control; make objective scoring and written suggestions explicit |
| Teaching Materials | Partial | Support text plus PDF import/reference; expose retrieval state |
| Local RAG | Partial | Add indexed chunks/retrieval flow and assistant grounding |
| Student Records | Present in latest workspace branch | Add durable records and class lookup without cloud sync |
| Local database / SQLite | Missing | Use a small local SQLite/Room-style repository or clearly document the lightweight store |
| Saved lessons/questions/worksheets/results | Partial | Centralize saved resource metadata and sharing/export |
| PDF export | Missing | Add device-local PDF generation/share path |
| CBT export | Missing | Add a structured plain-text/CSV export path suitable for later CBT import |
| Model manager/status | Partial | Improve model status and keep import user-triggered |
| Optional internet updates/downloads/sync/backups | Intentionally not implemented | These conflict with the current Android-only offline-first scope; leave extension points, not hidden network behavior |

## Acceptance interpretation

The core teacher experience must work without an imported model through deterministic offline drafts. When a compatible MediaPipe `.task` model is imported, generation should use it only in response to a teacher action. Local materials should be stored on-device and used as ranked context; the app must not claim semantic vector search unless an embedding/index implementation is actually present. Marking must remain advisory and editable by the teacher.
