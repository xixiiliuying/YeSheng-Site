package cc.feitwnd.service.impl;

import cc.feitwnd.dto.MomentsDTO;
import cc.feitwnd.dto.MomentsPageQueryDTO;
import cc.feitwnd.dto.MomentsSubmitDTO;
import cc.feitwnd.entity.Moments;
import cc.feitwnd.mapper.MomentsMapper;
import cc.feitwnd.result.PageResult;
import cc.feitwnd.service.MomentsService;
import cc.feitwnd.vo.MomentsVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MomentsServiceImpl implements MomentsService {

    @Autowired
    private MomentsMapper momentsMapper;

    // 卡片色系
    private static final String[] COLORS = {
        "#FFE4E1", "#E8F4FD", "#FFF8DC", "#F0FFF0", "#F5F0FF",
        "#FFF0F5", "#F0FFFF", "#FAFAD2", "#FFEFD5", "#E6E6FA"
    };

    @Override
    public PageResult pageQuery(MomentsPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<MomentsVO> page = (Page<MomentsVO>) momentsMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public MomentsVO getById(Long id) {
        return momentsMapper.getById(id);
    }

    @Override
    public void create(MomentsDTO dto) {
        Moments m = new Moments();
        BeanUtils.copyProperties(dto, m);
        m.setIsApproved(1); // 管理员发布直接通过
        if (dto.getColor() == null || dto.getColor().isEmpty()) {
            m.setColor(COLORS[(int) (Math.random() * COLORS.length)]);
        }
        momentsMapper.insert(m);
    }

    @Override
    public void update(MomentsDTO dto) {
        Moments m = new Moments();
        BeanUtils.copyProperties(dto, m);
        momentsMapper.update(m);
    }

    @Override
    public void delete(Long id) {
        momentsMapper.deleteById(id);
    }

    @Override
    public void approve(Long id, Integer isApproved) {
        momentsMapper.approve(id, isApproved);
    }

    @Override
    public MomentsVO randomOne() {
        return momentsMapper.randomOne();
    }

    @Override
    public List<MomentsVO> listApproved() {
        return momentsMapper.listApproved();
    }

    @Override
    public void submit(MomentsSubmitDTO dto, Long visitorId, String ip) {
        Moments m = new Moments();
        m.setContent(dto.getContent());
        m.setEmoji(dto.getEmoji());
        m.setNickname(dto.getNickname());
        m.setVisitorId(visitorId);
        m.setIp(ip);
        m.setIsApproved(0); // 待审核
        m.setIsVisible(1);
        m.setColor(COLORS[(int) (Math.random() * COLORS.length)]);
        momentsMapper.insert(m);
    }

    @Override
    public List<MomentsVO> myMoments(Long visitorId) {
        return momentsMapper.listByVisitorId(visitorId);
    }
}
