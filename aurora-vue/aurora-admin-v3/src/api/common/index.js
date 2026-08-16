import { request } from '@/utils/http'

// ========== 公共接口 ==========

// 获取关于我信息
export const getAboutApi = () => request.get('/api/about')

// 更新关于我信息
export const updateAboutApi = (data) => request.put('/api/admin/about', data)
