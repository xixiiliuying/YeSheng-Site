package cc.feitwnd.controller.blog;

import cc.feitwnd.dto.MomentsSubmitDTO;
import cc.feitwnd.result.Result;
import cc.feitwnd.service.MomentsService;
import cc.feitwnd.service.VisitorTokenService;
import cc.feitwnd.utils.IpUtil;
import cc.feitwnd.vo.MomentsVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("blogMomentsController")
@RequestMapping("/blog/moments")
public class MomentsController {

    @Autowired
    private MomentsService momentsService;
    @Autowired
    private VisitorTokenService visitorTokenService;

    /** 随机获取一条已通过的瞬间 */
    @GetMapping("/random")
    public Result<MomentsVO> randomOne() {
        MomentsVO vo = momentsService.randomOne();
        return Result.success(vo);
    }

    /** 获取所有已通过的瞬间 */
    @GetMapping
    public Result<List<MomentsVO>> listApproved() {
        return Result.success(momentsService.listApproved());
    }

    /** 访客发布瞬间 */
    @PostMapping
    public Result submit(@RequestBody MomentsSubmitDTO dto, HttpServletRequest request) {
        Long visitorId = visitorTokenService.resolveVisitorId(request);
        String ip = IpUtil.getClientIp(request);
        momentsService.submit(dto, visitorId, ip);
        return Result.success();
    }

    /** 访客查看自己发布的瞬间 */
    @GetMapping("/my")
    public Result<List<MomentsVO>> myMoments(HttpServletRequest request) {
        Long visitorId = visitorTokenService.resolveVisitorId(request);
        return Result.success(momentsService.myMoments(visitorId));
    }
}
