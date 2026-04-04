/**
 * 若存在 src/assets/default-avatar.jpg 则得到打包后的 URL，否则为 undefined（使用 SVG 占位）。
 */
const modules = import.meta.glob('../assets/default-avatar.jpg', { eager: true, as: 'url' })
export const defaultAvatarImageUrl = Object.values(modules)[0]
