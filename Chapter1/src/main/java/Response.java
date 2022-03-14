import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/*
  HTTP Response = Status-Line
    *(( general-header | response-header | entity-header ) CRLF)
    CRLF
    [ message-body ]
    Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
*/

public class Response {

  private static final int BUFFER_SIZE = 1024;
  private static final String DEFAULT_RESPONSE_HEAD = "HTTP/1.1 200 OK\r\n";
  Request request;
  OutputStream output;

  public Response(OutputStream output) {
    this.output = output;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  private String getFileExtension(String fileName){
    int idx = fileName.lastIndexOf(".");
    if(idx != -1){
      return fileName.substring(idx + 1);
    }
    throw new RuntimeException("[ERROR]: Illegal file name!");
  }

  private void setResponseHead(String resourceName, int contentLength) throws IOException {
    StringBuffer sb = new StringBuffer(DEFAULT_RESPONSE_HEAD);
    String resourceType = getFileExtension(resourceName);
    if(resourceType.equals("html") || resourceType.equals("htm")){
      sb.append("Content-Type: text/html; charset=UTF-8\r\n");
    }else if(resourceType.equals("gif") || resourceType.equals("jpeg") || resourceType.equals("png")){
      sb.append("Content-Type: image/").append(resourceType).append("\r\n");
    }else{
      throw new RuntimeException("[ERROR]: Unimplemented static resource type!");
    }
    sb.append("Content-Length: ").append(contentLength).append("\r\n\r\n");
    output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
  }

  public void sendStaticResource() throws IOException {
    byte[] bytes = new byte[BUFFER_SIZE];
    FileInputStream fis = null;
    try {
      File file = new File(HttpServer.WEB_ROOT, request.getUri());
      if (file.exists()) {
        fis = new FileInputStream(file);
        setResponseHead(file.getName(), fis.available());
        int ch = fis.read(bytes, 0, BUFFER_SIZE);
        while (ch!=-1) {
          output.write(bytes, 0, ch);
          ch = fis.read(bytes, 0, BUFFER_SIZE);
        }
      }
      else {
        // file not found
        String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
          "Content-Type: text/html\r\n" +
          "Content-Length: 23\r\n" +
          "\r\n" +
          "<h1>File Not Found</h1>";
        output.write(errorMessage.getBytes());
      }
    }
    catch (Exception e) {
      // thrown if cannot instantiate a File object
      System.out.println(e.toString() );
    }
    finally {
      if (fis!=null)
        fis.close();
    }
  }
}