package cc.feitwnd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomentsDTO {

    private Long id;
    private String content;
    private String emoji;
    private String color;
    private String tag;
    private String nickname;
    private Integer isApproved;
    private Integer isVisible;
}
