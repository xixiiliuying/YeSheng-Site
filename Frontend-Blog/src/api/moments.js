import request from '@/utils/request'

/** 随机一条瞬间 */
export const getRandomMoment = () => request.get('/blog/moments/random')

/** 全部已通过 */
export const getMomentsList = () => request.get('/blog/moments')

/** 访客发布 */
export const submitMoment = (data) => request.post('/blog/moments', data)

/** 我的投稿 */
export const getMyMoments = () => request.get('/blog/moments/my')
