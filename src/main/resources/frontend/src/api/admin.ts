import request from './request';

export function getBookingPage(params: any) {
  return request({
    url: '/api/admin/bookings/page',
    method: 'get',
    params
  });
}

export function approveBooking(id: number) {
  return request({
    url: `/api/admin/bookings/${id}/approve`,
    method: 'post'
  });
}

export function rejectBooking(id: number, data: any) {
  return request({
    url: `/api/admin/bookings/${id}/reject`,
    method: 'post',
    data
  });
}

export function getVenuePage(params: any) {
  return request({
    url: '/admin/venue/page',
    method: 'get',
    params
  });
}

export function saveVenue(data: any, userId?: number) {
  return request({
    url: '/admin/venue/save',
    method: 'post',
    data,
    params: { userId }
  });
}

export function deleteVenue(id: number, userId?: number) {
  return request({
    url: `/admin/venue/${id}`,
    method: 'delete',
    params: { userId }
  });
}

export function updateVenueStatus(data: any, userId?: number) {
  return request({
    url: '/admin/venue/status',
    method: 'put',
    data,
    params: { userId }
  });
}

export function getAuthorizedBuildings(userId: number) {
  return request({
    url: '/admin/venue/buildings',
    method: 'get',
    params: { userId }
  });
}

export function getVenueLocks(id: number) {
  return request({
    url: `/admin/venue/${id}/locks`,
    method: 'get'
  });
}

export function deleteVenueLock(lockId: number) {
  return request({
    url: `/admin/venue/locks/${lockId}`,
    method: 'delete'
  });
}


