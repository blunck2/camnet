package camnet.agent.engine;


import java.util.Map;

public class ImageRetrievalResponse {
    private byte[] content;
    private Map<String, String> headers;


    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
