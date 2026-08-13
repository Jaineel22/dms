// Thin re-export: the actual implementation already lives at
// src/components/common/Pagination.jsx (built earlier in this phase, before this
// specific file path was requested). Re-exporting here avoids maintaining two copies
// of the same component. If you'd rather standardize on one location, delete whichever
// path you don't want and update imports accordingly.
export { default } from '../common/Pagination';