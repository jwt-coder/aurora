import { request } from '@/utils/http'

// ========== 分类管理 ==========

// 获取分类列表
export const getCategoriesApi = (params) => request.get('/api/admin/categories', { params })

// 搜索分类
export const searchCategoriesApi = (params) => request.get('/api/admin/categories/search', { params })

// 保存或更新分类
export const saveCategoryApi = (data) => request.post('/api/admin/categories', data)

// 删除分类（批量删除）
export const deleteCategoryApi = (ids) => request.delete('/api/admin/categories', { data: ids })
