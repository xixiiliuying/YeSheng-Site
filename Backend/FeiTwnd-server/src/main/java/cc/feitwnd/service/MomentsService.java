package cc.feitwnd.service;

import cc.feitwnd.dto.MomentsDTO;
import cc.feitwnd.dto.MomentsPageQueryDTO;
import cc.feitwnd.dto.MomentsSubmitDTO;
import cc.feitwnd.result.PageResult;
import cc.feitwnd.vo.MomentsVO;

import java.util.List;

public interface MomentsService {

    /** 管理端分页 */
    PageResult pageQuery(MomentsPageQueryDTO dto);

    /** 管理端详情 */
    MomentsVO getById(Long id);

    /** 管理端新增 */
    void create(MomentsDTO dto);

    /** 管理端编辑 */
    void update(MomentsDTO dto);

    /** 管理端删除 */
    void delete(Long id);

    /** 审核 */
    void approve(Long id, Integer isApproved);

    /** 博客端：随机一条 */
    MomentsVO randomOne();

    /** 博客端：全部已通过 */
    List<MomentsVO> listApproved();

    /** 博客端：访客发布 */
    void submit(MomentsSubmitDTO dto, Long visitorId, String ip);

    /** 博客端：我的投稿 */
    List<MomentsVO> myMoments(Long visitorId);
}
