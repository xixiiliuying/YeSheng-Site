package cc.feitwnd.mapper;

import cc.feitwnd.annotation.AutoFill;
import cc.feitwnd.dto.MomentsPageQueryDTO;
import cc.feitwnd.entity.Moments;
import cc.feitwnd.enumeration.OperationType;
import cc.feitwnd.vo.MomentsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MomentsMapper {

    List<MomentsVO> pageQuery(MomentsPageQueryDTO dto);

    MomentsVO getById(Long id);

    /** 随机取一条已通过的瞬间 */
    MomentsVO randomOne();

    /** 获取所有已通过的瞬间 */
    List<MomentsVO> listApproved();

    /** 访客查看自己发布的 */
    List<MomentsVO> listByVisitorId(Long visitorId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Moments moments);

    @AutoFill(value = OperationType.UPDATE)
    void update(Moments moments);

    void deleteById(Long id);

    /** 审核 */
    void approve(Long id, Integer isApproved);
}
