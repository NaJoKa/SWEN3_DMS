package at.fhtw.swen.springbatchdemo.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "accessLog")
public class AccessLog {

    @JacksonXmlProperty(isAttribute = true)
    private String date;

    @JacksonXmlElementWrapper(localName = "documentAccesses")
    @JacksonXmlProperty(localName = "access")
    private List<DocumentAccessEvent> documentAccesses;
}