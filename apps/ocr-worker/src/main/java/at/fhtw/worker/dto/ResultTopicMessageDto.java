package at.fhtw.worker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultTopicMessageDto {
    private String processedMessage;
    private String objectKey;
    private String fileName;
}
