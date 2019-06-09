import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;

public class HttpServer extends Thread{
    /**
     * web资源根路径
     */
    public static final String ROOT = "D:/";

    /**
     * 输入流对象,读取浏览器请求
     */
    private InputStream inputStream;

    /**
     * 输出流对象，响应内容给浏览器
     */
    private OutputStream outputStream;

    public HttpServer(Socket socket) {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析请求
     * @return <头, 属性>键值对
     * @exampel <"method", "GET">
     */
    public HashMap<String, String> parseRequest(){
        HashMap<String, String> map = new HashMap<>();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try {
            String firstLine = bufferedReader.readLine();
            String[] firstLineElements = firstLine.split(" ");

            if(firstLineElements.length != 3){
                return null;
            }
            //请求方法
            map.put("method", firstLineElements[0]);

            //URL
            map.put("URL", firstLineElements[1]);

            //协议版本
            map.put("version", firstLineElements[2]);

            //body
            map.put("body", "");
            if(map.get("method").equals("POST")){
                String headline = bufferedReader.readLine();
                while (!headline.equals("\r\n") && !headline.equals("")) {
                    headline = bufferedReader.readLine();
                }
                String body = bufferedReader.readLine();
                System.out.println(body);
                map.put("body", body);
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
        return map;
    }

    public void putResponse(HashMap<String, String> map){
        final String CRLF = "\r\n";
        String method = map.get("method");
        String path = map.get("URL");
        String body = map.get("body");

        if(method.equals("GET")){
            doGet(path);
        }
        else if(method.equals("POST")){
            doPost(path, body);
        }
    }

    private void doGet(String filePath){
        File file = new File(ROOT + filePath);
        if (file.exists()) {
            // 1、资源存在，读取资源
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\r\n");
                }
                StringBuffer result = new StringBuffer();
                result.append("HTTP /1.1 200 ok /r/n");
                result.append("Content-Type:text/html /r/n");
                result.append("Content-Length:" + file.length() + "/r/n");
                result.append("\r\n:" + sb.toString());
                outputStream.write(result.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // 2、资源不存在，提示 file not found
            StringBuffer error = new StringBuffer();
            error.append("HTTP /1.1 400 file not found /r/n");
            error.append("Content-Type:text/html \r\n");
            error.append("Content-Length:20 \r\n").append("\r\n");
            error.append("<h1 >File Not Found..</h1>");
            try {
                outputStream.write(error.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doPost(String path, String body){
        System.out.println(body);
    }

    /**
     * 多线程调用这个类
     */
    @Override
    public void run(){
        HashMap<String, String> map = parseRequest();
        putResponse(map);
    }
}
