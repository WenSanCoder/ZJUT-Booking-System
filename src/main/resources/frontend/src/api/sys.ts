import request from './request';

export function getUserPage(params: any) {
  return request({
    url: '/api/sys/users/page',
    method: 'get',
    params
  });
}

export function saveUser(data: any) {
  return request({
    url: '/api/sys/users/save',
    method: 'post',
    data
  });
}

export function deleteUser(id: number) {
  return request({
    url: `/api/sys/users/${id}`,
    method: 'delete'
  });
}

export function notifyUser(data: any) {
  return request({
    url: '/api/sys/users/notify',
    method: 'post',
    data
  });
}

export function getBuildingList() {
  return request({
    url: '/api/sys/buildings/list',
    method: 'get'
  });
}

export function saveBuilding(data: any) {
  return request({
    url: '/api/sys/buildings/save',
    method: 'post',
    data
  });
}

export function deleteBuilding(id: number) {
  return request({
    url: `/api/sys/buildings/${id}`,
    method: 'delete'
  });
}

export function getAdminPermissions(adminId: number) {
  return request({
    url: `/api/sys/permissions/${adminId}`,
    method: 'get'
  });
}

export function assignPermissions(data: any) {
  return request({
    url: '/api/sys/permissions/assign',
    method: 'post',
    data
  });
}

export function publishAnnouncement(data: any) {
  return request({
    url: '/api/sys/announcements/publish',
    method: 'post',
    data
  });
}
