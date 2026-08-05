package cc.feitwnd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomentsPageQueryDTO {

    private Integer page;
    private Integer pageSize;
    private String tag;
    private Integer isApproved;
}
