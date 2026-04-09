/**
 * If src/assets/default-avatar.jpg exists, exposes its built URL; otherwise undefined (SVG fallback).
 */
const modules = import.meta.glob('../assets/default-avatar.jpg', {
  eager: true,
  query: '?url',
  import: 'default',
})
export const defaultAvatarImageUrl = Object.values(modules)[0]
