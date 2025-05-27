import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.net.URL;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CoinDatabase {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36";
    private static final HttpClient client = HttpClient.newBuilder().build();

    private Connection connect() {
        String url = "jdbc:sqlite:D:\\Aleks\\OpenNumismat\\coins1.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return conn;
    }

    public void insertCoin(String title, String value, String country, int year,
                           String material, String obverseTitle, String obverseImageUrl,
                           String reverseTitle, String reverseImageUrl) {
        // SQL-запити для вставки
        String insertPhotoSQL = "INSERT INTO photos(title, image) VALUES(?, ?)";
        String insertCoinSQL = "INSERT INTO Coins(title, value, country, year, material, obverseimg, reverseimg) VALUES(?, ?, ?, ?, ?, ?, ?)";

        byte[] obverseImage = null;
        byte[] reverseImage = null;
        try {
            obverseImage = downloadImage(obverseImageUrl);
            reverseImage = downloadImage(reverseImageUrl);
        } catch (IOException | InterruptedException e) {
            System.out.println("Помилка завантаження зображення: " + e.getMessage());
            return;
        }

        // Використовуйте try-with-resources для всіх PreparedStatement
        try (Connection conn = this.connect();



             PreparedStatement pstmtPhotoObverse = conn.prepareStatement(insertPhotoSQL, new String[] {"id"});
             PreparedStatement pstmtPhotoReverse = conn.prepareStatement(insertPhotoSQL, new String[] {"id"});
             PreparedStatement pstmtCoin = conn.prepareStatement(insertCoinSQL)) {
            // Вставка зображення аверсу
            pstmtPhotoObverse.setString(1, obverseTitle);
            pstmtPhotoObverse.setBytes(2, obverseImage);
            pstmtPhotoObverse.executeUpdate();

            // Отримання ID аверсу
            long obverseId = pstmtPhotoObverse.getGeneratedKeys().getLong(1);

            // Вставка зображення реверсу
            pstmtPhotoReverse.setString(1, reverseTitle);
            pstmtPhotoReverse.setBytes(2, reverseImage);
            pstmtPhotoReverse.executeUpdate();

            // Отримання ID реверсу
            long reverseId = pstmtPhotoReverse.getGeneratedKeys().getLong(1);

            // Вставка даних про монету
            pstmtCoin.setString(1, title);
            pstmtCoin.setString(2, value);
            pstmtCoin.setString(3, country);
            pstmtCoin.setInt(4, year);
            pstmtCoin.setString(5, material);
            pstmtCoin.setLong(6, obverseId);
            pstmtCoin.setLong(7, reverseId);
            pstmtCoin.executeUpdate();

            System.out.println("Coin inserted successfully");

        } catch (SQLException e) {
            System.out.println("Помилка вставки монети: " + e.getMessage());
        }
    }

    // Приклад використання
    public static void main(String[] args) {
        CoinDatabase db = new CoinDatabase();

        // Приклад читання зображень з файлів
        /*
        byte[] obverseImage = readImage("photos/31_obverse.jpg");
        byte[] reverseImage = readImage("photos/31_reverse.jpg");
*/

        String obverseUrl = "https://avtmarket.com.ua/sites/default/files/other_images/logo_2.svg ";
        String reverseUrl = "https://www.ua-coins.info/images/coins/big/2485_reverse.png";
        // Вставка монети
        db.insertCoin(
                "Життя",    // title
                "5 грн",               // value
                "Україна",            // country
                2020,             // year
                "цинк",         // material
                "Аверс",   // obverseTitle
                obverseUrl,     // obverseImage
                "Реверс",   // reverseTitle
                reverseUrl      // reverseImage
        );
    }

    // Метод для читання зображення з файлу
    private static byte[] readImage(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            System.out.println("Error reading image: " + e.getMessage());
            return null;
        }
    }

    // Метод для завантаження зображення з URL

    private static byte[] downloadImage(String imageUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            System.out.println("Не вдалося завантажити зображення: " + imageUrl);
            return null;
        }
    }

    /*
    private byte[] downloadImage(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            System.out.println("Error downloading image from " + imageUrl + ": " + e.getMessage());
            return null;
        }
    }*/
}
