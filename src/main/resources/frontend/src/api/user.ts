// api/user.ts
import request from './request';

export function login(data: any) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  });
}

export function register(data: any) {
  return request({
    url: '/users/register',
    method: 'post',
    data
  });
}

export function changePassword(data: any) {
  return request({
    url: '/users/password',
    method: 'post',
    data
  });
}

export function getInfo(id: number) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  });
}

