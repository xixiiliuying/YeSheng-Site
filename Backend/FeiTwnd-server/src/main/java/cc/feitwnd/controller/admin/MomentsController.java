package cc.feitwnd.controller.admin;

import cc.feitwnd.annotation.OperationLog;
import cc.feitwnd.dto.MomentsDTO;
import cc.feitwnd.dto.MomentsPageQueryDTO;
import cc.feitwnd.enumeration.OperationType;
import cc.feitwnd.result.PageResult;
import cc.feitwnd.result.Result;
import cc.feitwnd.service.MomentsService;
import cc.feitwnd.vo.MomentsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminMomentsController")
@RequestMapping("/admin/moments")
public class MomentsController {

    @Autowired
    private MomentsService momentsService;

    @GetMapping("/page")
    public Result<PageResult> pageQuery(MomentsPageQueryDTO dto) {
        return Result.success(momentsService.pageQuery(dto));
    }

    @GetMapping("/{id}")
    public Result<MomentsVO> getById(@PathVariable Long id) {
        return Result.success(momentsService.getById(id));
    }

    @PostMapping
    @OperationLog(value = OperationType.INSERT, target = "moments")
    public Result create(@RequestBody MomentsDTO dto) {
        momentsService.create(dto);
        return Result.success();
    }

    @PutMapping
    @OperationLog(value = OperationType.UPDATE, target = "moments", targetId = "#dto.id")
    public Result update(@RequestBody MomentsDTO dto) {
        momentsService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog(value = OperationType.DELETE, target = "moments", targetId = "#id")
    public Result delete(@PathVariable Long id) {
        momentsService.delete(id);
        return Result.success();
    }

    @PutMapping("/approve/{id}")
    @OperationLog(value = OperationType.UPDATE, target = "moments", targetId = "#id")
    public Result approve(@PathVariable Long id, @RequestParam Integer isApproved) {
        momentsService.approve(id, isApproved);
        return Result.success();
    }
}
