import request from './request';

export function getLatestAnnouncements(params?: { limit: number }) {
  return request({
    url: '/announcement/latest',
    method: 'get',
    params
  });
}

export function getAnnouncementPage(params: any) {
  return request({
    url: '/announcement/page',
    method: 'get',
    params
  });
}

export function getAnnouncementById(id: number) {
  return request({
    url: `/announcement/${id}`,
    method: 'get'
  });
}

export function publishAnnouncement(data: any) {
  return request({
    url: '/announcement/publish',
    method: 'post',
    data
  });
}

export function updateAnnouncement(data: any) {
  return request({
    url: '/announcement/update',
    method: 'post',
    data
  });
}

export function deleteAnnouncement(id: number) {
  return request({
    url: `/announcement/${id}`,
    method: 'delete'
  });
}
