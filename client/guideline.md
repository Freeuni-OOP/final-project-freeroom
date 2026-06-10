# Client-side project guidelines

## Folder Structure

- `src/pages/` - full pages rendered by the router
- `src/components/` - reusable UI pieces used across pages
- `src/services/` - all external communication (Firebase, API calls)
- `src/services/firebase/` - Firebase init and auth logic
- `src/services/api/` - axios instance and all API endpoint functions
- `src/utils/` - shared helper functions

## File Naming

- Each page or component lives in its own folder: `ComponentName/ComponentName.jsx`
- The custom hook for it lives next to it: `ComponentName/useComponentName.js`
- Every folder has an `index.js` that exports the main thing from that folder
- Hook files use `.js`, component/page files use `.jsx`

## JSX Files Have No Logic

- `.jsx` files only import their own hook and render JSX, nothing else
- No `useState`, `useEffect`, `fetch`, or any logic directly in `.jsx` files
- All logic goes into the custom hook for that file

## Custom Hooks

- Every page and component has its own hook named `use<ComponentName>`
- The hook returns a plain object with everything the JSX needs
- The hook lives in the same folder as the component

## Imports

- Use `@/` for absolute imports, for example `import { LandingPage } from '@/pages'`
- Import pages from `@/pages`, components from `@/components`, services from `@/services`
- No relative `../../` style imports

## Services

- All API calls go in `src/services/api/endpoints.js` using the axios instance
- The axios instance in `axiosInstance.js` handles auth tokens and errors globally
- Firebase config and auth functions stay in `src/services/firebase/`
- Do not call Firebase or axios directly from components or pages

## General Rules

- No descriptive comments in the code
- No unused imports
- When adding a new component, follow the same folder structure: folder, JSX file, hook file, index.js
