package at.fhtw.worker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrTopicMessageDto {
    @JsonProperty("objectKey")
    private String objectKey;
    @JsonProperty("bucket")
    private String bucket;

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Override
    public String toString() {
        return "OcrTopicMessageDto{" +
               "objectKey='" + objectKey + '\'' +
               ", bucket='" + bucket + '\'' +
               '}';
    }
}
