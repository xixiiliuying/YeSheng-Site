import request from '@/utils/request'

/** 随机一条瞬间 */
export const getRandomMoment = () => request.get('/blog/moments/random')

/** 全部已通过 */
export const getMomentsList = () => request.get('/blog/moments')

/** 访客发布 */
export const submitMoment = (data, visitorToken, visitorFingerprint) =>
  request.post('/blog/moments', data, {
    headers: {
      'X-Visitor-Token': visitorToken || '',
      'X-Visitor-Fingerprint': visitorFingerprint || ''
    }
  })

/** 我的投稿 */
export const getMyMoments = (visitorToken, visitorFingerprint) =>
  request.get('/blog/moments/my', {
    headers: {
      'X-Visitor-Token': visitorToken || '',
      'X-Visitor-Fingerprint': visitorFingerprint || ''
    }
  })
