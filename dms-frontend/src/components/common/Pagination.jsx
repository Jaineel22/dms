// ─────────────────────────────────────────────────────────────────────────────
// Pagination.jsx (common)
// Compatibility re-export.
//
// The real implementation lives at `../ui/Pagination.jsx` and is used by the
// working pages (e.g. UserList). Several newer pages import it from
// `../../components/common/Pagination`, so this file re-exports the UI version
// to keep a single source of truth.
// ─────────────────────────────────────────────────────────────────────────────

export { default } from '../ui/Pagination';
