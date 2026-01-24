package at.fhtw.worker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultTopicMessageDto {
    @JsonProperty("objectKey")
    private String objectKey;
    @JsonProperty("bucket")
    private String bucket;
    @JsonProperty("text")
    private String text;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "ResultTopicMessageDto{" +
               "objectKey='" + objectKey + '\'' +
               ", bucket='" + bucket + '\'' +
               ", text lth=" + (text != null ? text.length() : 0) +
               '}';
    }
}
