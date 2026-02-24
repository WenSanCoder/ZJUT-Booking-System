import request from './request';

export function getBookingPage(params: any) {
  return request({
    url: '/admin/bookings/page',
    method: 'get',
    params
  });
}

export function approveBooking(id: number) {
  return request({
    url: `/admin/bookings/${id}/approve`,
    method: 'post'
  });
}

export function rejectBooking(id: number, reason: string) {
  return request({
    url: `/admin/bookings/${id}/reject`,
    method: 'post',
    data: { reason }
  });
}

export function getVenuePage(params: any) {
  return request({
    url: '/admin/venue/page',
    method: 'get',
    params
  });
}

export function saveVenue(data: any) {
  return request({
    url: '/admin/venue/save',
    method: 'post',
    data
  });
}

export function deleteVenue(id: number) {
  return request({
    url: `/admin/venue/${id}`,
    method: 'delete'
  });
}

export function updateVenueStatus(data: any) {
  return request({
    url: '/admin/venue/status',
    method: 'put',
    data
  });
}


