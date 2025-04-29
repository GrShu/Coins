import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;

public class CoinDatabase {
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
                           String material, String obverseTitle, byte[] obverseImage,
                           String reverseTitle, byte[] reverseImage) {
        // SQL-запити для вставки
        String insertPhotoSQL = "INSERT INTO photos(title, image) VALUES(?, ?)";
        String insertCoinSQL = "INSERT INTO Coins(title, value, country, year, material, obverseimg, reverseimg) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = this.connect()) {
            // Вставка зображення аверсу
            PreparedStatement pstmtPhotoObverse = conn.prepareStatement(insertPhotoSQL,
                    new String[] {"id"});
            pstmtPhotoObverse.setString(1, obverseTitle);
            pstmtPhotoObverse.setBytes(2, obverseImage);
            pstmtPhotoObverse.executeUpdate();

            // Отримання ID аверсу
            long obverseId = pstmtPhotoObverse.getGeneratedKeys().getLong(1);

            // Вставка зображення реверсу
            PreparedStatement pstmtPhotoReverse = conn.prepareStatement(insertPhotoSQL,
                    new String[] {"id"});
            pstmtPhotoReverse.setString(1, reverseTitle);
            pstmtPhotoReverse.setBytes(2, reverseImage);
            pstmtPhotoReverse.executeUpdate();

            // Отримання ID реверсу
            long reverseId = pstmtPhotoReverse.getGeneratedKeys().getLong(1);

            // Вставка даних про монету
            PreparedStatement pstmtCoin = conn.prepareStatement(insertCoinSQL);
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
            System.out.println("Error inserting coin: " + e.getMessage());
        }
    }

    // Приклад використання
    public static void main(String[] args) {
        CoinDatabase db = new CoinDatabase();

        // Приклад читання зображень з файлів
        byte[] obverseImage = readImage("photos/31_obverse.jpg");
        byte[] reverseImage = readImage("photos/31_reverse.jpg");

        // Вставка монети
        db.insertCoin(
                "Леся",    // title
                "200 грн",               // value
                "Україна",            // country
                1997,             // year
                "Серебро",         // material
                "Аверс",   // obverseTitle
                obverseImage,     // obverseImage
                "Реверс",   // reverseTitle
                reverseImage      // reverseImage
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
}
