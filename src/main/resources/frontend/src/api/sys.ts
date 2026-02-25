import request from './request';

export function getUserPage(params: any) {
  return request({
    url: '/sys/users/page',
    method: 'get',
    params
  });
}

export function saveUser(data: any) {
  return request({
    url: '/sys/users/save',
    method: 'post',
    data
  });
}

export function deleteUser(id: number) {
  return request({
    url: `/sys/users/${id}`,
    method: 'delete'
  });
}

export function notifyUser(data: any) {
  return request({
    url: '/sys/users/notify',
    method: 'post',
    data
  });
}

export function getBuildingList() {
  return request({
    url: '/sys/buildings/list',
    method: 'get'
  });
}

export function saveBuilding(data: any) {
  return request({
    url: '/sys/buildings/save',
    method: 'post',
    data
  });
}

export function deleteBuilding(id: number) {
  return request({
    url: `/sys/buildings/${id}`,
    method: 'delete'
  });
}

export function getAdminPermissions(adminId: number) {
  return request({
    url: `/sys/permissions/${adminId}`,
    method: 'get'
  });
}

export function assignPermissions(data: any) {
  return request({
    url: '/sys/permissions/assign',
    method: 'post',
    data
  });
}

// --- 公告管理功能已迁移至 announcement.ts ---
