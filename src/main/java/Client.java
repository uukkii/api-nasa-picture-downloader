import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Client {

    private static final String REMOTE_NASA_URL = "https://api.nasa.gov/planetary/apod?api_key=ZMB9Nz5kALfj1AYM2NejepUp0yN9lkD4zDEBhdI8";
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setConnectTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        HttpGet request = new HttpGet(REMOTE_NASA_URL);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = httpClient.execute(request);

        NasaPic nasaPic = mapper.readValue(response.getEntity().getContent(), new TypeReference<NasaPic>() {
        });
        String picUrl = nasaPic.getUrl();
        String fileName = picUrl.substring(picUrl.lastIndexOf('/') + 1);

//                  Сохранение изображения через Java NIO
        URL url = new URL(picUrl);
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        readableByteChannel.close();

//                  Сохранение изображения через буфер
//        try (BufferedInputStream in = new BufferedInputStream(new URL(picUrl).openStream());
//             FileOutputStream outputStream = new FileOutputStream(fileName)) {
//            byte dataBuffer[] = new byte[Byte.MAX_VALUE];
//            int byteReader;
//            while ((byteReader = in.read(dataBuffer, 0, Byte.MAX_VALUE)) != -1) {
//                outputStream.write(dataBuffer, 0, byteReader);
//            }
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//        InputStream in = new URL(picUrl).openStream();
//        Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
//        in.close();

        response.close();
        httpClient.close();
    }
}
